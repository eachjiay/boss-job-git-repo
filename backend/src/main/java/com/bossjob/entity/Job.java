package com.bossjob.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 职位实体类
 */
@Data
@TableName("jobs")
public class Job {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布职位的公司ID
     */
    private Long companyId;

    /**
     * 发布者用户ID
     */
    private Long publisherId;

    /**
     * 职位名称
     */
    private String title;

    /**
     * 职位类型: 社招全职, 应届校园招聘, 实习生招聘, 兼职招聘
     */
    private String jobType;

    /**
     * 工作城市
     */
    private String city;

    /**
     * 工作地点详细地址
     */
    private String address;

    /**
     * 最低薪资 (K)
     */
    private Integer salaryMin;

    /**
     * 最高薪资 (K)
     */
    private Integer salaryMax;

    /**
     * 经验要求: 不限, 在校/应届, 1年以内, 1-3年, 3-5年, 5-10年, 10年以上
     */
    private String experience;

    /**
     * 学历要求: 不限, 大专, 本科, 硕士, 博士
     */
    private String education;

    /**
     * 职位描述
     */
    @TableField("`description`")
    private String description;

    /**
     * 岗位职责
     */
    private String responsibility;

    /**
     * 任职要求
     */
    private String requirement;

    /**
     * 职位福利标签，JSON数组格式
     */
    private String benefits;

    /**
     * 职位关键词，JSON数组格式
     */
    private String keywords;

    /**
     * 职位状态: 0-已下线, 1-招聘中
     */
    private Integer status;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
