package com.bossjob.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 职位列表VO
 */
@Data
public class JobListVO {

    private Long id;
    private String title;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private String experience;
    private String education;
    private String[] keywords;
    private String[] benefits;

    // 公司信息
    private Long companyId;
    private String companyName;
    private String companyLogo;
    private String companyScale;
    private String industry;
    private String financing;

    // 发布者信息
    private String publisherName;
    private String publisherTitle;

    private LocalDateTime createTime;
}
