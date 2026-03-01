package com.bossjob.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 职位申请实体类
 */
@Data
@TableName("applications")
public class Application {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请的职位ID
     */
    private Long jobId;

    /**
     * 申请者用户ID
     */
    private Long userId;

    /**
     * 申请者简历ID
     */
    private Long resumeId;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 申请状态:
     * PENDING(待处理),
     * VIEWED(已查看),
     * CHATTING(沟通中),
     * INTERVIEW(已约面),
     * OFFER(已发offer),
     * HIRED(已入职),
     * REJECTED(不合适)
     */
    private String status;

    /**
     * 招聘者备注
     */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
