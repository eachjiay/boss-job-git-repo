package com.bossjob.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bossjob.dto.MessageDTO;
import com.bossjob.entity.Message;
import com.bossjob.vo.ConversationVO;
import com.bossjob.vo.MessageVO;

import java.util.List;

public interface MessageService extends IService<Message> {

    // 发送消息
    MessageVO sendMessage(Long senderId, MessageDTO messageDTO);

    // 获取会话列表
    List<ConversationVO> getConversations(Long userId);

    // 获取与某人的聊天记录
    List<MessageVO> getHistory(Long userId, Long otherId);

    // 获取与某人的聊天记录 (分页)
    List<MessageVO> getHistoryPaged(Long userId, Long otherId, int page, int size);

    // 获取与某人的聊天记录总数
    long getHistoryCount(Long userId, Long otherId);

    // 标记消息为已读
    void markAsRead(Long userId, Long otherId);

    // 获取未读消息总数
    Integer getUnreadCount(Long userId);
}
