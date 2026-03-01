package com.bossjob.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageVO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer isRead;
    private String attachmentUrl; // 附件地址
    private Integer messageType; // 消息类型: 1=文本, 2=简历申请, 3=系统提示

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // 额外字段，用于前端判断是自己发的还是对方发的
    private Boolean isMine;
    private String senderAvatar;
    private String senderName;
}
