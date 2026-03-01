package com.bossjob.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bossjob.common.PageResult;
import com.bossjob.dto.MessageDTO;
import com.bossjob.entity.*;
import com.bossjob.mapper.ApplicationMapper;
import com.bossjob.service.*;
import com.bossjob.vo.ApplicationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

    private final UserService userService;
    private final JobService jobService;
    private final ResumeService resumeService;
    private final CompanyService companyService;
    private final MessageService messageService;

    @Override
    public Application apply(Long jobId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        // 检查是否已投递
        if (hasApplied(jobId)) {
            throw new RuntimeException("您已投递过该职位");
        }

        // 获取简历
        Resume resume = resumeService.getMyResume();
        if (resume == null) {
            throw new RuntimeException("请先完善简历");
        }

        // 获取职位信息
        Job job = jobService.getById(jobId);
        if (job == null) {
            throw new RuntimeException("职位不存在");
        }

        Application application = new Application();
        application.setJobId(jobId);
        application.setUserId(currentUser.getId());
        application.setResumeId(resume.getId());
        application.setCompanyId(job.getCompanyId());
        application.setStatus("PENDING");
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());

        save(application);

        // 自动发送消息给招聘者，附带简历附件（如果有）
        try {
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setReceiverId(job.getPublisherId());
            messageDTO.setContent("您好，我对贵公司的「" + job.getTitle() + "」职位很感兴趣，已投递简历，请查收！");
            messageDTO.setMessageType(2); // RESUME_REQUEST 类型

            // 如果有附件简历，一并发送
            if (StringUtils.hasText(resume.getAttachmentUrl())) {
                messageDTO.setAttachmentUrl(resume.getAttachmentUrl());
            }

            messageService.sendMessage(currentUser.getId(), messageDTO);
        } catch (Exception e) {
            // 消息发送失败不影响投递主流程
            log.warn("Auto-send message failed: " + e.getMessage());
        }

        return application;
    }

    @Override
    public PageResult<ApplicationVO> getMyApplications(Integer page, Integer size) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        Page<Application> appPage = page(
                new Page<>(page, size),
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getUserId, currentUser.getId())
                        .orderByDesc(Application::getCreateTime));

        List<ApplicationVO> voList = appPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(appPage.getTotal(), voList, appPage.getCurrent(), appPage.getSize());
    }

    @Override
    public PageResult<ApplicationVO> getReceivedApplications(String status, Integer page, Integer size) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        Company company = companyService.getByUserId(currentUser.getId());
        if (company == null) {
            throw new RuntimeException("请先完善公司信息");
        }

        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<Application>()
                .eq(Application::getCompanyId, company.getId())
                .orderByDesc(Application::getCreateTime);

        if (StringUtils.hasText(status)) {
            wrapper.eq(Application::getStatus, status);
        }

        Page<Application> appPage = page(new Page<>(page, size), wrapper);

        List<ApplicationVO> voList = appPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(appPage.getTotal(), voList, appPage.getCurrent(), appPage.getSize());
    }

    @Override
    public Application updateStatus(Long id, String status) {
        Application application = getById(id);
        if (application == null) {
            throw new RuntimeException("申请记录不存在");
        }

        application.setStatus(status);
        application.setUpdateTime(LocalDateTime.now());
        updateById(application);

        return application;
    }

    @Override
    public boolean hasApplied(Long jobId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return count(new LambdaQueryWrapper<Application>()
                .eq(Application::getJobId, jobId)
                .eq(Application::getUserId, currentUser.getId())) > 0;
    }

    private ApplicationVO convertToVO(Application application) {
        ApplicationVO vo = new ApplicationVO();
        vo.setId(application.getId());
        vo.setJobId(application.getJobId());
        vo.setUserId(application.getUserId());
        vo.setResumeId(application.getResumeId());
        vo.setCompanyId(application.getCompanyId());
        vo.setStatus(application.getStatus());
        vo.setRemark(application.getRemark());
        vo.setCreateTime(application.getCreateTime());
        vo.setUpdateTime(application.getUpdateTime());

        // 职位信息
        Job job = jobService.getById(application.getJobId());
        if (job != null) {
            vo.setJobTitle(job.getTitle());
            vo.setJobCity(job.getCity());
            vo.setJobSalaryMin(job.getSalaryMin());
            vo.setJobSalaryMax(job.getSalaryMax());
        }

        // 公司信息
        Company company = companyService.getById(application.getCompanyId());
        if (company != null) {
            vo.setCompanyName(company.getName());
            vo.setCompanyLogo(company.getLogo());
        }

        // 求职者信息
        Resume resume = resumeService.getById(application.getResumeId());
        if (resume != null) {
            vo.setCandidateName(resume.getRealName());
            vo.setCandidateAvatar(resume.getAvatar());
            vo.setCandidateEducation(resume.getEducation());
            vo.setCandidateSchool(resume.getSchool());
            vo.setCandidateWorkYears(resume.getWorkYears());
            vo.setCandidateAge(resume.getAge());
            vo.setCandidateExpectPosition(resume.getExpectPosition());
        }

        return vo;
    }
}
