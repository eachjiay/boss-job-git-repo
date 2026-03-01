package com.bossjob.controller;

import com.bossjob.common.Result;
import com.bossjob.dto.MessageDTO;
import com.bossjob.entity.User;
import com.bossjob.service.MessageService;
import com.bossjob.service.UserService;
import com.bossjob.vo.ConversationVO;
import com.bossjob.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    // 发送消息
    @PostMapping
    public Result<MessageVO> sendMessage(@RequestBody MessageDTO messageDTO) {
        User currentUser = userService.getCurrentUser();
        MessageVO messageVO = messageService.sendMessage(currentUser.getId(), messageDTO);

        // 通过 WebSocket 推送给接收者
        messagingTemplate.convertAndSendToUser(
                messageDTO.getReceiverId().toString(),
                "/queue/messages",
                messageVO);

        return Result.success(messageVO);
    }

    // 获取会话列表
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> getConversations() {
        User currentUser = userService.getCurrentUser();
        return Result.success(messageService.getConversations(currentUser.getId()));
    }

    // 获取与某人的聊天记录
    @GetMapping("/{otherId}")
    public Result<List<MessageVO>> getHistory(@PathVariable Long otherId) {
        User currentUser = userService.getCurrentUser();
        return Result.success(messageService.getHistory(currentUser.getId(), otherId));
    }

    // 获取与某人的聊天记录 (分页)
    @GetMapping("/{otherId}/paged")
    public Result<java.util.Map<String, Object>> getHistoryPaged(
            @PathVariable Long otherId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        User currentUser = userService.getCurrentUser();
        List<MessageVO> messages = messageService.getHistoryPaged(currentUser.getId(), otherId, page, size);
        long total = messageService.getHistoryCount(currentUser.getId(), otherId);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("records", messages);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("hasMore", (long) page * size < total);

        return Result.success(result);
    }

    // 获取未读消息数
    @GetMapping("/unread")
    public Result<Integer> getUnreadCount() {
        User currentUser = userService.getCurrentUser();
        return Result.success(messageService.getUnreadCount(currentUser.getId()));
    }

    // 发起沟通 (一键发送初始消息并返回会话信息)
    @PostMapping("/start-conversation")
    public Result<MessageVO> startConversation(@RequestBody MessageDTO messageDTO) {
        User currentUser = userService.getCurrentUser();
        // 直接复用 sendMessage，content 可以是自动生成的打招呼语
        if (messageDTO.getContent() == null || messageDTO.getContent().isEmpty()) {
            messageDTO.setContent("您好，我对这个职位很感兴趣，希望能进一步沟通！");
        }
        MessageVO messageVO = messageService.sendMessage(currentUser.getId(), messageDTO);

        // 通过 WebSocket 推送给接收者
        messagingTemplate.convertAndSendToUser(
                messageDTO.getReceiverId().toString(),
                "/queue/messages",
                messageVO);

        return Result.success(messageVO);
    }
}
