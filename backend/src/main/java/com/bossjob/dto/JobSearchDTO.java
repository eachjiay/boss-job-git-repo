package com.bossjob.dto;

import lombok.Data;

/**
 * 职位搜索请求DTO
 */
@Data
public class JobSearchDTO {

    /**
     * 关键词搜索
     */
    private String keyword;

    /**
     * 城市
     */
    private String city;

    /**
     * 行业
     */
    private String industry;

    /**
     * 职位类型
     */
    private String jobType;

    /**
     * 经验要求
     */
    private String experience;

    /**
     * 学历要求
     */
    private String education;

    /**
     * 最低薪资
     */
    private Integer salaryMin;

    /**
     * 最高薪资
     */
    private Integer salaryMax;

    /**
     * 公司规模
     */
    private String companyScale;

    /**
     * 融资阶段
     */
    private String financing;

    /**
     * 当前页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 排序方式: default, latest, salary
     */
    private String sort = "default";
}
