package com.bossjob.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageDTO {
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    private String content; // 文本内容 (附件消息时可为空)

    private String attachmentUrl; // 附件地址 (可选)

    private Integer messageType; // 消息类型: 1=文本, 2=简历申请, 3=系统提示
}
