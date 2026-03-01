package com.bossjob.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bossjob.dto.MessageDTO;
import com.bossjob.entity.Conversation;
import com.bossjob.entity.Company;
import com.bossjob.entity.Message;
import com.bossjob.entity.Resume;
import com.bossjob.entity.User;
import com.bossjob.enums.MessageType;
import com.bossjob.mapper.MessageMapper;
import com.bossjob.service.CompanyService;
import com.bossjob.service.ConversationService;
import com.bossjob.service.MessageService;
import com.bossjob.service.ResumeService;
import com.bossjob.service.UserService;
import com.bossjob.vo.ConversationVO;
import com.bossjob.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final UserService userService;
    private final CompanyService companyService;
    private final ResumeService resumeService;
    private final ConversationService conversationService;

    @Override
    public MessageVO sendMessage(Long senderId, MessageDTO messageDTO) {
        // 检查接收者是否存在
        User receiver = userService.getById(messageDTO.getReceiverId());
        if (receiver == null) {
            throw new RuntimeException("接收用户不存在");
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(messageDTO.getReceiverId());
        message.setContent(messageDTO.getContent());
        message.setAttachmentUrl(messageDTO.getAttachmentUrl()); // 设置附件地址
        // 设置消息类型，默认为文本消息
        message.setMessageType(
                messageDTO.getMessageType() != null ? messageDTO.getMessageType() : MessageType.TEXT.getCode());
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());

        save(message);

        // 更新会话表
        conversationService.updateLastMessage(senderId, messageDTO.getReceiverId(), message);
        conversationService.incrementUnreadCount(senderId, messageDTO.getReceiverId());

        return convertToVO(message, senderId);
    }

    @Override
    public List<ConversationVO> getConversations(Long userId) {
        // 使用会话表优化查询
        List<Conversation> conversationList = conversationService.list(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserIdA, userId)
                        .or()
                        .eq(Conversation::getUserIdB, userId)
                        .orderByDesc(Conversation::getLastMessageTime));

        List<ConversationVO> result = new ArrayList<>();
        for (Conversation conv : conversationList) {
            // 确定对方用户ID
            Long otherId = conv.getUserIdA().equals(userId) ? conv.getUserIdB() : conv.getUserIdA();

            // 确定当前用户的未读数
            int unreadCount = conv.getUserIdA().equals(userId)
                    ? (conv.getUnreadCountA() != null ? conv.getUnreadCountA() : 0)
                    : (conv.getUnreadCountB() != null ? conv.getUnreadCountB() : 0);

            ConversationVO vo = new ConversationVO();
            vo.setOtherSideUserId(otherId);
            vo.setLastMessage(conv.getLastMessageContent());
            vo.setLastMessageTime(conv.getLastMessageTime());
            vo.setUnreadCount(unreadCount);

            // 填充对方信息
            fillOtherSideInfo(vo, otherId);
            result.add(vo);
        }

        return result;
    }

    @Override
    public List<MessageVO> getHistory(Long userId, Long otherId) {
        List<Message> messages = list(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getSenderId, userId).eq(Message::getReceiverId, otherId))
                .or(w -> w.eq(Message::getSenderId, otherId).eq(Message::getReceiverId, userId))
                .orderByAsc(Message::getCreateTime));

        // 标记对方发给我的消息为已读
        markAsRead(userId, otherId);

        return messages.stream()
                .map(msg -> convertToVO(msg, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageVO> getHistoryPaged(Long userId, Long otherId, int page, int size) {
        // 先获取总数用于计算偏移量
        long total = getHistoryCount(userId, otherId);

        // 计算偏移量 (从最新的消息往前取)
        // page=1 表示最新的一页，page=2 表示次新的一页
        long offset = Math.max(0, total - (long) page * size);
        int actualSize = (int) Math.min(size, total - (page - 1) * (long) size);

        if (actualSize <= 0) {
            return new ArrayList<>();
        }

        List<Message> messages = list(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getSenderId, userId).eq(Message::getReceiverId, otherId))
                .or(w -> w.eq(Message::getSenderId, otherId).eq(Message::getReceiverId, userId))
                .orderByAsc(Message::getCreateTime)
                .last("LIMIT " + actualSize + " OFFSET " + offset));

        // 第一页时标记消息为已读
        if (page == 1) {
            markAsRead(userId, otherId);
        }

        return messages.stream()
                .map(msg -> convertToVO(msg, userId))
                .collect(Collectors.toList());
    }

    @Override
    public long getHistoryCount(Long userId, Long otherId) {
        return count(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getSenderId, userId).eq(Message::getReceiverId, otherId))
                .or(w -> w.eq(Message::getSenderId, otherId).eq(Message::getReceiverId, userId)));
    }

    @Override
    public void markAsRead(Long userId, Long otherId) {
        update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Message>()
                .eq(Message::getSenderId, otherId)
                .eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1));

        // 同步清除会话表中的未读计数
        conversationService.clearUnreadCount(userId, otherId);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        return (int) count(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0));
    }

    private MessageVO convertToVO(Message message, Long currentUserId) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setContent(message.getContent());
        vo.setAttachmentUrl(message.getAttachmentUrl()); // 附件地址
        vo.setMessageType(message.getMessageType()); // 消息类型
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());
        vo.setIsMine(message.getSenderId().equals(currentUserId));

        User sender = userService.getById(message.getSenderId());
        if (sender != null) {
            vo.setSenderName(sender.getRealName());
            vo.setSenderAvatar(sender.getAvatar());
        }

        return vo;
    }

    private void fillOtherSideInfo(ConversationVO vo, Long otherId) {
        User user = userService.getById(otherId);
        if (user != null) {
            vo.setOtherSideUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            vo.setOtherSideAvatar(user.getAvatar());
            vo.setOtherSideRole(user.getRole());

            if ("RECRUITER".equals(user.getRole())) {
                Company company = companyService.getByUserId(otherId);
                if (company != null) {
                    vo.setOtherSideCompany(company.getName());
                    vo.setOtherSideAvatar(company.getLogo()); // 招聘者优先显示公司Logo
                } else {
                    vo.setOtherSideCompany("未绑定公司");
                }
            } else {
                // 求职者，显示期望职位
                Resume resume = resumeService.getByUserId(otherId);
                if (resume != null) {
                    vo.setOtherSideCompany(resume.getExpectPosition()); // 这里复用company字段显示职位
                } else {
                    vo.setOtherSideCompany("求职者");
                }
            }
        }
    }
}
