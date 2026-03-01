package com.bossjob.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 求职者简历实体类
 */
@Data
@TableName("resumes")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的用户ID
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 性别: 男, 女
     */
    private String gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 最高学历
     */
    private String education;

    /**
     * 毕业院校
     */
    private String school;

    /**
     * 专业
     */
    private String major;

    /**
     * 毕业年份
     */
    private Integer graduationYear;

    /**
     * 工作年限
     */
    private String workYears;

    /**
     * 求职状态: 离职-随时到岗, 在职-考虑机会, 在职-暂不考虑, 在校-月内到岗
     */
    private String jobStatus;

    /**
     * 期望城市
     */
    private String expectCity;

    /**
     * 期望职位
     */
    private String expectPosition;

    /**
     * 期望薪资最低 (K)
     */
    private Integer expectSalaryMin;

    /**
     * 期望薪资最高 (K)
     */
    private Integer expectSalaryMax;

    /**
     * 个人优势/自我介绍
     */
    private String introduction;

    /**
     * 专业技能，JSON数组格式
     */
    private String skills;

    /**
     * 工作经历，JSON数组格式
     */
    private String workExperience;

    /**
     * 项目经历，JSON数组格式
     */
    private String projectExperience;

    /**
     * 教育经历，JSON数组格式
     */
    private String educationExperience;

    /**
     * 附件简历地址
     */
    private String attachmentUrl;

    /**
     * 实习经历，JSON数组格式
     */
    private String internshipExperience;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
