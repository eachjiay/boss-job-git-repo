package com.bossjob.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bossjob.entity.Company;
import com.bossjob.entity.Job;
import com.bossjob.entity.User;
import com.bossjob.mapper.CompanyMapper;
import com.bossjob.mapper.JobMapper;
import com.bossjob.mapper.UserMapper;
import com.bossjob.service.CompanyService;
import com.bossjob.vo.CompanyDetailVO;
import com.bossjob.vo.JobListVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company> implements CompanyService {

    private final JobMapper jobMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    public CompanyDetailVO getCompanyDetail(Long id) {
        Company company = getById(id);
        if (company == null) {
            throw new RuntimeException("公司不存在");
        }

        CompanyDetailVO vo = new CompanyDetailVO();
        vo.setId(company.getId());
        vo.setName(company.getName());
        vo.setLogo(company.getLogo());
        vo.setScale(company.getScale());
        vo.setIndustry(company.getIndustry());
        vo.setFinancing(company.getFinancing());
        vo.setDescription(company.getDescription());
        vo.setDetail(company.getDetail());
        vo.setAddress(company.getAddress());
        vo.setCity(company.getCity());
        vo.setBusinessInfo(company.getBusinessInfo());
        vo.setCreateTime(company.getCreateTime());

        // 解析JSON数组
        vo.setBenefits(parseJsonArray(company.getBenefits()));
        vo.setImages(parseJsonArray(company.getImages()));

        // 统计职位数量
        Long jobCount = jobMapper.selectCount(new LambdaQueryWrapper<Job>()
                .eq(Job::getCompanyId, id)
                .eq(Job::getStatus, 1));
        vo.setJobCount(jobCount.intValue());

        // 统计HR数量
        Long bossCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "RECRUITER"));
        vo.setBossCount(bossCount.intValue());

        // 获取热门职位
        List<Job> hotJobs = jobMapper.selectList(new LambdaQueryWrapper<Job>()
                .eq(Job::getCompanyId, id)
                .eq(Job::getStatus, 1)
                .orderByDesc(Job::getViewCount)
                .last("LIMIT 3"));

        List<JobListVO> jobListVOs = new ArrayList<>();
        for (Job job : hotJobs) {
            JobListVO jobVO = new JobListVO();
            jobVO.setId(job.getId());
            jobVO.setTitle(job.getTitle());
            jobVO.setCity(job.getCity());
            jobVO.setSalaryMin(job.getSalaryMin());
            jobVO.setSalaryMax(job.getSalaryMax());
            jobVO.setExperience(job.getExperience());
            jobVO.setCompanyId(company.getId());
            jobVO.setCompanyName(company.getName());
            jobListVOs.add(jobVO);
        }
        vo.setHotJobs(jobListVOs);

        return vo;
    }

    @Override
    public Company createCompany(Company company) {
        company.setCreateTime(LocalDateTime.now());
        company.setUpdateTime(LocalDateTime.now());
        save(company);
        return company;
    }

    @Override
    public Company updateCompany(Company company) {
        company.setUpdateTime(LocalDateTime.now());
        updateById(company);
        return getById(company.getId());
    }

    @Override
    public Company getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<Company>()
                .eq(Company::getUserId, userId));
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
