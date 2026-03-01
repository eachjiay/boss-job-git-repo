package com.bossjob.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bossjob.entity.Conversation;
import com.bossjob.entity.Message;

public interface ConversationService extends IService<Conversation> {

    /**
     * 获取或创建两个用户之间的会话
     */
    Conversation getOrCreateConversation(Long userId1, Long userId2);

    /**
     * 更新会话的最后消息信息
     */
    void updateLastMessage(Long userId1, Long userId2, Message message);

    /**
     * 增加某用户的未读计数
     */
    void incrementUnreadCount(Long senderId, Long receiverId);

    /**
     * 清除某用户的未读计数
     */
    void clearUnreadCount(Long userId, Long otherId);

    /**
     * 获取两个用户之间的会话
     */
    Conversation getConversation(Long userId1, Long userId2);
}
