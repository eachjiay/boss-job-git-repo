package com.bossjob.controller;

import com.bossjob.dto.MessageDTO;
import com.bossjob.service.MessageService;
import com.bossjob.service.UserService;
import com.bossjob.entity.User;
import com.bossjob.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket 消息控制器
 * 处理实时消息发送
 */
@Controller
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 处理客户端发送的消息
     * 客户端发送到: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageDTO messageDTO, Principal principal) {
        if (principal == null) {
            return;
        }

        // 获取当前用户
        User sender = userService.getByUsername(principal.getName());
        Long senderId = sender.getId();

        // 获取接收者信息以获取用户名 (WebSocket Principal 是用户名)
        User receiver = userService.getById(messageDTO.getReceiverId());
        String receiverUsername = receiver.getUsername();

        // 如果是通话信令消息 (type >= 4)，直接转发不保存数据库
        if (messageDTO.getMessageType() != null && messageDTO.getMessageType() >= 4) {
            // 封装信令消息，包含发送者基本信息，方便对方展示来电弹窗
            MessageVO signalingMsg = new MessageVO();
            signalingMsg.setSenderId(senderId);
            signalingMsg.setReceiverId(messageDTO.getReceiverId());
            signalingMsg.setContent(messageDTO.getContent()); // 保持原始信令内容(SDP/Candidates/Invite)
            signalingMsg.setMessageType(messageDTO.getMessageType());
            signalingMsg.setSenderName(sender.getRealName());
            signalingMsg.setSenderAvatar(sender.getAvatar());
            signalingMsg.setIsMine(false);

            messagingTemplate.convertAndSendToUser(
                    receiverUsername,
                    "/queue/messages",
                    signalingMsg);

            // --- 新增：生成通话记录系统消息 ---
            String systemContent = null;
            if (messageDTO.getMessageType() == 5) { // ACCEPT
                // 判断是视频还是语音 (根据 invite 无法直接得知，但 Accept 消息本身没有携带 info?
                // 实际上 Accept 消息是对 Invite 的响应。
                // 简单点：如果 Inviter 发送 Invite_Audio，Receiver Accept.
                // 但 WebSocketMessageController 这里收到的 messageDTO 是 "ACCEPT"。
                // 只有 content 是 "ACCEPT" ?
                // 此时无法区分是 Video 还是 Audio 接受??

                // 修正：前端 Accept 时，可以携带 "ACCEPT_AUDIO" 或 "ACCEPT_VIDEO"
                // 我需要修改前端 handleAcceptCall 发送的消息内容。 (Let's stick to generic for now or updated
                // frontend first?)

                // 让我先修改前端发送 Accept 的 content, 然后再改后端。
                // 但为了不中断, 我可以先统一写 "通话已接通"
                // 或者: 前端 INVITE_AUDIO -> 这里的 messageDTO 是 INVITE。

                // Oops. This block handles ACCEPT (Type 5).
                // If I want to log "Voice Call Connected", I need to know it was Voice.

                // Let's modify frontend to send content: 'ACCEPT_AUDIO' if isAudioCall.

                if ("ACCEPT_AUDIO".equals(messageDTO.getContent())) {
                    systemContent = "语音通话已接通";
                } else {
                    systemContent = "视频通话已接通"; // Default/Fallback
                }
            } else if (messageDTO.getMessageType() == 6) {
                if ("REJECT".equals(messageDTO.getContent())) {
                    systemContent = "通话已拒绝";
                } else if ("CANCEL".equals(messageDTO.getContent())) {
                    systemContent = "已取消呼叫";
                } else if ("BUSY".equals(messageDTO.getContent())) {
                    systemContent = "对方忙线中";
                } else if ("HANGUP".equals(messageDTO.getContent())) {
                    systemContent = "通话结束";
                }
            }

            if (systemContent != null) {
                MessageDTO sysMsg = new MessageDTO();
                sysMsg.setReceiverId(messageDTO.getReceiverId());
                sysMsg.setMessageType(3); // 系统消息
                sysMsg.setContent(systemContent);

                // 保存并获取完整消息对象
                MessageVO sysMsgVO = messageService.sendMessage(senderId, sysMsg);
                // 设置发送者信息 (虽然系统消息不显示头像，但为了完整性)
                sysMsgVO.setSenderName(sender.getRealName());
                sysMsgVO.setSenderAvatar(sender.getAvatar());

                // 推送给接收者
                messagingTemplate.convertAndSendToUser(
                        receiverUsername,
                        "/queue/messages",
                        sysMsgVO);

                // 推送给发送者
                messagingTemplate.convertAndSendToUser(
                        sender.getUsername(),
                        "/queue/messages",
                        sysMsgVO);
            }
            // ------------------------------------

            return;
        }

        // 保存消息到数据库
        MessageVO messageVO = messageService.sendMessage(senderId, messageDTO);

        // 推送给接收者 (点对点消息)
        // 接收者订阅: /user/queue/messages
        messagingTemplate.convertAndSendToUser(
                receiverUsername,
                "/queue/messages",
                messageVO);

        // 同时推送给发送者 (用于确认消息已发送)
        messagingTemplate.convertAndSendToUser(
                sender.getUsername(),
                "/queue/messages",
                messageVO);
    }

    /**
     * 通知用户有新消息 (用于更新未读计数)
     */
    public void notifyNewMessage(Long userId, MessageVO messageVO) {
        User user = userService.getById(userId);
        if (user != null) {
            messagingTemplate.convertAndSendToUser(
                    user.getUsername(),
                    "/queue/notifications",
                    messageVO);
        }
    }
}
