package com.bossjob.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公司详情VO
 */
@Data
public class CompanyDetailVO {

    private Long id;
    private String name;
    private String logo;
    private String scale;
    private String industry;
    private String financing;
    private String description;
    private String detail;
    private String[] benefits;
    private String address;
    private String city;
    private String[] images;
    private String businessInfo;

    // 统计信息
    private Integer jobCount;
    private Integer bossCount;

    // 热门职位
    private java.util.List<JobListVO> hotJobs;

    private LocalDateTime createTime;
}
