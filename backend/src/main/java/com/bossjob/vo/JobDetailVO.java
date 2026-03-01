package com.bossjob.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 职位详情VO
 */
@Data
public class JobDetailVO {

    private Long id;
    private String title;
    private String jobType;
    private String city;
    private String address;
    private Integer salaryMin;
    private Integer salaryMax;
    private String experience;
    private String education;
    private String description;
    private String responsibility;
    private String requirement;
    private String[] keywords;
    private String[] benefits;
    private Integer viewCount;
    private Integer status;

    // 公司信息
    private Long companyId;
    private String companyName;
    private String companyLogo;
    private String companyScale;
    private String industry;
    private String financing;
    private String companyDescription;

    // 发布者信息
    private Long publisherId;
    private String publisherName;
    private String publisherAvatar;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 是否已投递
    private Boolean hasApplied;
}
