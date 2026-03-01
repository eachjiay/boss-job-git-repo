package com.bossjob.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("messages")
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long senderId;

    private Long receiverId;

    private String content;

    private Integer isRead; // 0: 未读, 1: 已读

    private String attachmentUrl; // 附件地址 (可选)

    /**
     * 消息类型: 1=文本消息, 2=简历申请, 3=系统提示
     */
    private Integer messageType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
