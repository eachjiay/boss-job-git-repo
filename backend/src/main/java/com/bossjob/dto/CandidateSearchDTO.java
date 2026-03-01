package com.bossjob.dto;

import lombok.Data;

/**
 * 候选人搜索请求DTO (HR端)
 */
@Data
public class CandidateSearchDTO {

    /**
     * 关键词搜索
     */
    private String keyword;

    /**
     * 城市
     */
    private String city;

    /**
     * 学历
     */
    private String education;

    /**
     * 工作年限
     */
    private String workYears;

    /**
     * 年龄范围 - 最小
     */
    private Integer ageMin;

    /**
     * 年龄范围 - 最大
     */
    private Integer ageMax;

    /**
     * 求职状态
     */
    private String jobStatus;

    /**
     * 毕业院校类型: 985, 211, 双一流
     */
    private String schoolType;

    /**
     * 期望薪资最低
     */
    private Integer salaryMin;

    /**
     * 期望薪资最高
     */
    private Integer salaryMax;

    /**
     * 当前页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 排序方式
     */
    private String sort = "default";
}
