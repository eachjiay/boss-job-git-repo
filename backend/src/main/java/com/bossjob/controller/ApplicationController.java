package com.bossjob.controller;

import com.bossjob.common.PageResult;
import com.bossjob.common.Result;
import com.bossjob.entity.Application;
import com.bossjob.service.ApplicationService;
import com.bossjob.vo.ApplicationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final com.bossjob.service.AiResumeService aiResumeService;

    @PostMapping("/apply/{jobId}")
    public Result<Application> apply(@PathVariable Long jobId) {
        Application application = applicationService.apply(jobId);
        return Result.success("投递成功", application);
    }

    @GetMapping("/my")
    public Result<PageResult<ApplicationVO>> getMyApplications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ApplicationVO> result = applicationService.getMyApplications(page, size);
        return Result.success(result);
    }

    @GetMapping("/received")
    public Result<PageResult<ApplicationVO>> getReceivedApplications(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ApplicationVO> result = applicationService.getReceivedApplications(status, page, size);
        return Result.success(result);
    }

    @PutMapping("/{id}/status")
    public Result<Application> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Application application = applicationService.updateStatus(id, status);
        return Result.success("状态更新成功", application);
    }

    @GetMapping("/check/{jobId}")
    public Result<Boolean> hasApplied(@PathVariable Long jobId) {
        boolean hasApplied = applicationService.hasApplied(jobId);
        return Result.success(hasApplied);
    }

    /**
     * 获取 AI 匹配度分析
     */
    @GetMapping("/{id}/ai-match")
    public Result<String> getAiMatch(@PathVariable Long id) {
        Application app = applicationService.getById(id);
        if (app == null)
            return Result.error("未找到申请记录");
        String result = aiResumeService.calculateMatchScore(app.getJobId(), app.getResumeId());
        return Result.success("分析成功", result);
    }
}
