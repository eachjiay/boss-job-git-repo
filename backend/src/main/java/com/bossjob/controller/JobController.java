package com.bossjob.controller;

import com.bossjob.common.PageResult;
import com.bossjob.common.Result;
import com.bossjob.dto.JobPublishDTO;
import com.bossjob.dto.JobSearchDTO;
import com.bossjob.entity.Job;
import com.bossjob.service.JobService;
import com.bossjob.vo.JobDetailVO;
import com.bossjob.vo.JobListVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final ObjectMapper objectMapper;

    @GetMapping("/search")
    public Result<PageResult<JobListVO>> searchJobs(JobSearchDTO searchDTO) {
        PageResult<JobListVO> result = jobService.searchJobs(searchDTO);
        return Result.success(result);
    }

    @GetMapping("/hot")
    public Result<PageResult<JobListVO>> getHotJobs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<JobListVO> result = jobService.getHotJobs(page, size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<JobDetailVO> getJobDetail(@PathVariable Long id) {
        JobDetailVO detail = jobService.getJobDetail(id);
        return Result.success(detail);
    }

    @GetMapping("/company/{companyId}")
    public Result<PageResult<JobListVO>> getJobsByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<JobListVO> result = jobService.getJobsByCompany(companyId, page, size);
        return Result.success(result);
    }

    @PostMapping
    public Result<Job> publishJob(@RequestBody JobPublishDTO dto) throws JsonProcessingException {
        Job job = convertDtoToJob(dto);
        Job created = jobService.publishJob(job);
        return Result.success("发布成功", created);
    }

    @PutMapping("/{id}")
    public Result<Job> updateJob(@PathVariable Long id, @RequestBody JobPublishDTO dto) throws JsonProcessingException {
        Job job = convertDtoToJob(dto);
        job.setId(id);
        Job updated = jobService.updateJob(job);
        return Result.success("更新成功", updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> offlineJob(@PathVariable Long id) {
        jobService.offlineJob(id);
        return Result.success("下线成功", null);
    }

    /**
     * 将DTO转换为Job实体，List<String> 转为 JSON字符串
     */
    private Job convertDtoToJob(JobPublishDTO dto) throws JsonProcessingException {
        Job job = new Job();
        job.setId(dto.getId());
        job.setTitle(dto.getTitle());
        job.setJobType(dto.getJobType());
        job.setCity(dto.getCity());
        job.setAddress(dto.getAddress());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        job.setExperience(dto.getExperience());
        job.setEducation(dto.getEducation());
        job.setDescription(dto.getDescription());
        job.setResponsibility(dto.getResponsibility());
        job.setRequirement(dto.getRequirement());

        // 将 List<String> 转换为 JSON 字符串
        if (dto.getBenefits() != null) {
            job.setBenefits(objectMapper.writeValueAsString(dto.getBenefits()));
        }
        if (dto.getKeywords() != null) {
            job.setKeywords(objectMapper.writeValueAsString(dto.getKeywords()));
        }

        return job;
    }
}
