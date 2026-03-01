package com.bossjob.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位收藏实体
 */
@Data
@TableName("favorites")
public class Favorite implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 收藏用户ID
     */
    private Long userId;

    /**
     * 收藏的职位ID
     */
    private Long jobId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
