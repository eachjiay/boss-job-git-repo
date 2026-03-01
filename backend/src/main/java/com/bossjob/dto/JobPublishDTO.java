package com.bossjob.dto;

import lombok.Data;
import java.util.List;

/**
 * 职位发布/编辑DTO
 * 用于接收前端发送的职位数据
 */
@Data
public class JobPublishDTO {
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

    /**
     * 职位福利标签，前端发送数组
     */
    private List<String> benefits;

    /**
     * 职位关键词，前端发送数组
     */
    private List<String> keywords;
}
