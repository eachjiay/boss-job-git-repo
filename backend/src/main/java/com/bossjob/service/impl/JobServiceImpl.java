package com.bossjob.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bossjob.common.PageResult;
import com.bossjob.dto.JobSearchDTO;
import com.bossjob.entity.Company;
import com.bossjob.entity.Job;
import com.bossjob.entity.User;
import com.bossjob.mapper.JobMapper;
import com.bossjob.service.ApplicationService;
import com.bossjob.service.CompanyService;
import com.bossjob.service.JobService;
import com.bossjob.service.UserService;
import com.bossjob.vo.JobDetailVO;
import com.bossjob.vo.JobListVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService {

    private final CompanyService companyService;
    private final UserService userService;
    private final ApplicationService applicationService;
    private final ObjectMapper objectMapper;

    public JobServiceImpl(
            CompanyService companyService,
            UserService userService,
            @Lazy ApplicationService applicationService,
            ObjectMapper objectMapper) {
        this.companyService = companyService;
        this.userService = userService;
        this.applicationService = applicationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResult<JobListVO> searchJobs(JobSearchDTO searchDTO) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            wrapper.and(w -> w
                    .like(Job::getTitle, searchDTO.getKeyword())
                    .or()
                    .like(Job::getDescription, searchDTO.getKeyword())
                    .or()
                    .like(Job::getKeywords, searchDTO.getKeyword()));
        }

        // 城市筛选
        if (StringUtils.hasText(searchDTO.getCity())) {
            wrapper.eq(Job::getCity, searchDTO.getCity());
        }

        // 经验要求
        if (StringUtils.hasText(searchDTO.getExperience())) {
            wrapper.eq(Job::getExperience, searchDTO.getExperience());
        }

        // 学历要求
        if (StringUtils.hasText(searchDTO.getEducation())) {
            wrapper.eq(Job::getEducation, searchDTO.getEducation());
        }

        // 薪资范围
        if (searchDTO.getSalaryMin() != null) {
            wrapper.ge(Job::getSalaryMax, searchDTO.getSalaryMin());
        }
        if (searchDTO.getSalaryMax() != null) {
            wrapper.le(Job::getSalaryMin, searchDTO.getSalaryMax());
        }

        // 只查询在线职位
        wrapper.eq(Job::getStatus, 1);

        // 排序
        switch (searchDTO.getSort()) {
            case "latest":
                wrapper.orderByDesc(Job::getCreateTime);
                break;
            case "salary":
                wrapper.orderByDesc(Job::getSalaryMax);
                break;
            default:
                wrapper.orderByDesc(Job::getViewCount);
        }

        Page<Job> page = page(
                new Page<>(searchDTO.getPage(), searchDTO.getSize()),
                wrapper);

        List<JobListVO> voList = page.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList, page.getCurrent(), page.getSize());
    }

    @Override
    public JobDetailVO getJobDetail(Long id) {
        Job job = getById(id);
        if (job == null) {
            throw new RuntimeException("职位不存在");
        }

        // 增加浏览次数
        job.setViewCount(job.getViewCount() == null ? 1 : job.getViewCount() + 1);
        updateById(job);

        return convertToDetailVO(job);
    }

    @Override
    public Job publishJob(Job job) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        Company company = companyService.getByUserId(currentUser.getId());
        if (company == null) {
            throw new RuntimeException("请先完善公司信息");
        }

        job.setCompanyId(company.getId());
        job.setPublisherId(currentUser.getId());
        job.setStatus(1);
        job.setViewCount(0);
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());

        save(job);
        return job;
    }

    @Override
    public Job updateJob(Job job) {
        job.setUpdateTime(LocalDateTime.now());
        updateById(job);
        return getById(job.getId());
    }

    @Override
    public void offlineJob(Long id) {
        Job job = getById(id);
        if (job == null) {
            throw new RuntimeException("职位不存在");
        }
        job.setStatus(0);
        job.setUpdateTime(LocalDateTime.now());
        updateById(job);
    }

    @Override
    public PageResult<JobListVO> getHotJobs(Integer page, Integer size) {
        Page<Job> jobPage = page(
                new Page<>(page, size),
                new LambdaQueryWrapper<Job>()
                        .eq(Job::getStatus, 1)
                        .orderByDesc(Job::getViewCount));

        List<JobListVO> voList = jobPage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return new PageResult<>(jobPage.getTotal(), voList, jobPage.getCurrent(), jobPage.getSize());
    }

    @Override
    public PageResult<JobListVO> getJobsByCompany(Long companyId, Integer page, Integer size) {
        Page<Job> jobPage = page(
                new Page<>(page, size),
                new LambdaQueryWrapper<Job>()
                        .eq(Job::getCompanyId, companyId)
                        .eq(Job::getStatus, 1)
                        .orderByDesc(Job::getCreateTime));

        List<JobListVO> voList = jobPage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return new PageResult<>(jobPage.getTotal(), voList, jobPage.getCurrent(), jobPage.getSize());
    }

    private JobListVO convertToListVO(Job job) {
        JobListVO vo = new JobListVO();
        vo.setId(job.getId());
        vo.setTitle(job.getTitle());
        vo.setCity(job.getCity());
        vo.setSalaryMin(job.getSalaryMin());
        vo.setSalaryMax(job.getSalaryMax());
        vo.setExperience(job.getExperience());
        vo.setEducation(job.getEducation());
        vo.setCreateTime(job.getCreateTime());

        // 解析JSON数组
        vo.setKeywords(parseJsonArray(job.getKeywords()));
        vo.setBenefits(parseJsonArray(job.getBenefits()));

        // 获取公司信息
        Company company = companyService.getById(job.getCompanyId());
        if (company != null) {
            vo.setCompanyId(company.getId());
            vo.setCompanyName(company.getName());
            vo.setCompanyLogo(company.getLogo());
            vo.setCompanyScale(company.getScale());
            vo.setIndustry(company.getIndustry());
            vo.setFinancing(company.getFinancing());
        }

        // 获取发布者信息
        User publisher = userService.getById(job.getPublisherId());
        if (publisher != null) {
            vo.setPublisherName(publisher.getRealName() != null ? publisher.getRealName() : publisher.getUsername());
        }

        return vo;
    }

    private JobDetailVO convertToDetailVO(Job job) {
        JobDetailVO vo = new JobDetailVO();
        vo.setId(job.getId());
        vo.setTitle(job.getTitle());
        vo.setJobType(job.getJobType());
        vo.setCity(job.getCity());
        vo.setAddress(job.getAddress());
        vo.setSalaryMin(job.getSalaryMin());
        vo.setSalaryMax(job.getSalaryMax());
        vo.setExperience(job.getExperience());
        vo.setEducation(job.getEducation());
        vo.setDescription(job.getDescription());
        vo.setResponsibility(job.getResponsibility());
        vo.setRequirement(job.getRequirement());
        vo.setViewCount(job.getViewCount());
        vo.setStatus(job.getStatus());
        vo.setCreateTime(job.getCreateTime());
        vo.setUpdateTime(job.getUpdateTime());

        vo.setKeywords(parseJsonArray(job.getKeywords()));
        vo.setBenefits(parseJsonArray(job.getBenefits()));

        // 公司信息
        Company company = companyService.getById(job.getCompanyId());
        if (company != null) {
            vo.setCompanyId(company.getId());
            vo.setCompanyName(company.getName());
            vo.setCompanyLogo(company.getLogo());
            vo.setCompanyScale(company.getScale());
            vo.setIndustry(company.getIndustry());
            vo.setFinancing(company.getFinancing());
            vo.setCompanyDescription(company.getDescription());
        }

        // 发布者信息
        User publisher = userService.getById(job.getPublisherId());
        if (publisher != null) {
            vo.setPublisherId(publisher.getId());
            vo.setPublisherName(publisher.getRealName() != null ? publisher.getRealName() : publisher.getUsername());
            vo.setPublisherAvatar(publisher.getAvatar());
        }

        // 检查是否已投递
        vo.setHasApplied(applicationService.hasApplied(job.getId()));

        return vo;
    }

    private String[] parseJsonArray(String json) {
        if (!StringUtils.hasText(json)) {
            return new String[0];
        }
        try {
            return objectMapper.readValue(json, String[].class);
        } catch (JsonProcessingException e) {
            return new String[0];
        }
    }
}
