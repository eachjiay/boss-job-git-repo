package com.bossjob.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公司实体类
 */
@Data
@TableName("companies")
public class Company {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的招聘者用户ID
     */
    private Long userId;

    private String name;

    private String logo;

    /**
     * 公司规模: 0-20人, 20-99人, 100-499人, 500-999人, 1000人以上
     */
    private String scale;

    /**
     * 行业类型
     */
    private String industry;

    /**
     * 融资阶段: 未融资, 天使轮, A轮, B轮, C轮, D轮及以上, 已上市, 不需要融资
     */
    private String financing;

    /**
     * 公司简介
     */
    @TableField("`description`")
    private String description;

    /**
     * 公司详细介绍
     */
    private String detail;

    /**
     * 公司福利标签，JSON数组格式
     */
    private String benefits;

    /**
     * 公司地址
     */
    private String address;

    /**
     * 城市
     */
    private String city;

    /**
     * 公司图片，JSON数组格式
     */
    private String images;

    /**
     * 工商信息
     */
    private String businessInfo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
