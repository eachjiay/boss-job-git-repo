package com.bossjob.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bossjob.entity.Conversation;
import com.bossjob.entity.Message;
import com.bossjob.mapper.ConversationMapper;
import com.bossjob.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation>
        implements ConversationService {

    @Override
    public Conversation getOrCreateConversation(Long userId1, Long userId2) {
        // 确保 userIdA < userIdB
        Long userIdA = Math.min(userId1, userId2);
        Long userIdB = Math.max(userId1, userId2);

        Conversation conversation = getConversation(userIdA, userIdB);
        if (conversation == null) {
            conversation = new Conversation();
            conversation.setUserIdA(userIdA);
            conversation.setUserIdB(userIdB);
            conversation.setUnreadCountA(0);
            conversation.setUnreadCountB(0);
            conversation.setCreateTime(LocalDateTime.now());
            conversation.setUpdateTime(LocalDateTime.now());
            save(conversation);
        }
        return conversation;
    }

    @Override
    public void updateLastMessage(Long userId1, Long userId2, Message message) {
        Long userIdA = Math.min(userId1, userId2);
        Long userIdB = Math.max(userId1, userId2);

        Conversation conversation = getOrCreateConversation(userIdA, userIdB);

        // 截断消息内容用于预览
        String contentPreview = message.getContent();
        if (contentPreview != null && contentPreview.length() > 100) {
            contentPreview = contentPreview.substring(0, 100) + "...";
        }

        update(new LambdaUpdateWrapper<Conversation>()
                .eq(Conversation::getId, conversation.getId())
                .set(Conversation::getLastMessageId, message.getId())
                .set(Conversation::getLastMessageContent, contentPreview)
                .set(Conversation::getLastMessageTime, message.getCreateTime())
                .set(Conversation::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public void incrementUnreadCount(Long senderId, Long receiverId) {
        Long userIdA = Math.min(senderId, receiverId);
        Long userIdB = Math.max(senderId, receiverId);

        Conversation conversation = getOrCreateConversation(userIdA, userIdB);

        // 接收者是A还是B
        if (receiverId.equals(userIdA)) {
            // 接收者是A，增加A的未读计数
            update(new LambdaUpdateWrapper<Conversation>()
                    .eq(Conversation::getId, conversation.getId())
                    .setSql("unread_count_a = unread_count_a + 1"));
        } else {
            // 接收者是B，增加B的未读计数
            update(new LambdaUpdateWrapper<Conversation>()
                    .eq(Conversation::getId, conversation.getId())
                    .setSql("unread_count_b = unread_count_b + 1"));
        }
    }

    @Override
    public void clearUnreadCount(Long userId, Long otherId) {
        Long userIdA = Math.min(userId, otherId);
        Long userIdB = Math.max(userId, otherId);

        Conversation conversation = getConversation(userIdA, userIdB);
        if (conversation == null)
            return;

        // 清除当前用户的未读计数
        if (userId.equals(userIdA)) {
            update(new LambdaUpdateWrapper<Conversation>()
                    .eq(Conversation::getId, conversation.getId())
                    .set(Conversation::getUnreadCountA, 0));
        } else {
            update(new LambdaUpdateWrapper<Conversation>()
                    .eq(Conversation::getId, conversation.getId())
                    .set(Conversation::getUnreadCountB, 0));
        }
    }

    @Override
    public Conversation getConversation(Long userId1, Long userId2) {
        Long userIdA = Math.min(userId1, userId2);
        Long userIdB = Math.max(userId1, userId2);

        return getOne(new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUserIdA, userIdA)
                .eq(Conversation::getUserIdB, userIdB));
    }
}
