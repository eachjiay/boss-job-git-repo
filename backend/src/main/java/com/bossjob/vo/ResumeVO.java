package com.bossjob.vo;

import lombok.Data;

/**
 * 简历VO
 */
@Data
public class ResumeVO {

    private Long id;
    private Long userId;
    private String realName;
    private String gender;
    private Integer age;
    private String phone;
    private String email;
    private String avatar;
    private String education;
    private String school;
    private String major;
    private Integer graduationYear;
    private String workYears;
    private String jobStatus;
    private String expectCity;
    private String expectPosition;
    private Integer expectSalaryMin;
    private Integer expectSalaryMax;
    private String introduction;
    private String[] skills;

    /**
     * 附件简历地址
     */
    private String attachmentUrl;

    // JSON字符串形式的经历
    private String workExperience;
    private String projectExperience;
    private String educationExperience;
}
