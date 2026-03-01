package com.bossjob.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话实体 - 用于优化会话列表查询
 * 记录两个用户之间的会话元数据
 */
@Data
@TableName("conversations")
public class Conversation implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户A的ID (较小的ID)
     */
    private Long userIdA;

    /**
     * 用户B的ID (较大的ID)
     */
    private Long userIdB;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 最后一条消息内容 (用于快速显示)
     */
    private String lastMessageContent;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * A对B的未读消息数
     */
    private Integer unreadCountA;

    /**
     * B对A的未读消息数
     */
    private Integer unreadCountB;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
