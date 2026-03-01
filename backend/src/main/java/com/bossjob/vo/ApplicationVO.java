package com.bossjob.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 投递记录VO
 */
@Data
public class ApplicationVO {

    private Long id;
    private Long jobId;
    private Long userId;
    private Long resumeId;
    private Long companyId;
    private String status;
    private String remark;

    // 职位信息
    private String jobTitle;
    private String jobCity;
    private Integer jobSalaryMin;
    private Integer jobSalaryMax;

    // 公司信息
    private String companyName;
    private String companyLogo;

    // 求职者信息 (HR端显示)
    private String candidateName;
    private String candidateAvatar;
    private String candidateEducation;
    private String candidateSchool;
    private String candidateWorkYears;
    private Integer candidateAge;
    private String candidateExpectPosition;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
