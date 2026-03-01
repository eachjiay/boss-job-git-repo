package com.bossjob.controller;

import com.bossjob.common.PageResult;
import com.bossjob.common.Result;
import com.bossjob.dto.CandidateSearchDTO;
import com.bossjob.entity.Resume;
import com.bossjob.service.AiResumeService;
import com.bossjob.service.ResumeService;
import com.bossjob.vo.ResumeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final AiResumeService aiResumeService;

    @GetMapping("/my")
    public Result<Resume> getMyResume() {
        Resume resume = resumeService.getMyResume();
        return Result.success(resume);
    }

    @PostMapping
    public Result<Resume> saveOrUpdateResume(@RequestBody Resume resume) {
        Resume saved = resumeService.saveOrUpdateResume(resume);
        return Result.success("保存成功", saved);
    }

    @GetMapping("/{id}")
    public Result<ResumeVO> getResumeDetail(@PathVariable Long id) {
        ResumeVO vo = resumeService.getResumeDetail(id);
        return Result.success(vo);
    }

    @GetMapping("/search")
    public Result<PageResult<ResumeVO>> searchCandidates(CandidateSearchDTO searchDTO) {
        PageResult<ResumeVO> result = resumeService.searchCandidates(searchDTO);
        return Result.success(result);
    }

    /**
     * 获取求职者附件简历地址
     * 
     * @param userId 求职者用户ID
     */
    @GetMapping("/attachment/{userId}")
    public Result<String> getResumeAttachment(@PathVariable Long userId) {
        Resume resume = resumeService.getByUserId(userId);
        if (resume == null || resume.getAttachmentUrl() == null) {
            return Result.error("该求职者未上传附件简历");
        }
        return Result.success(resume.getAttachmentUrl());
    }

    /**
     * AI解析附件简历
     * 上传简历后，调用此接口使用AI自动解析并返回结构化数据
     * 
     * @param request 包含attachmentUrl的请求体
     * @return 解析后的简历数据
     */
    @PostMapping("/parse-attachment")
    public Result<Resume> parseResumeAttachment(@RequestBody Map<String, String> request) {
        String attachmentUrl = request.get("attachmentUrl");
        if (attachmentUrl == null || attachmentUrl.isEmpty()) {
            return Result.error("请先上传简历附件");
        }

        try {
            Resume parsedResume = aiResumeService.parseResumeAttachment(attachmentUrl);
            return Result.success("解析成功", parsedResume);
        } catch (Exception e) {
            return Result.error("解析失败: " + e.getMessage());
        }
    }

    /**
     * 获取求职者 AI 分析建议 (核心优势 + 面试问题)
     * 
     * @param userId 求职者用户ID
     */
    @GetMapping("/ai-assist/{userId}")
    public Result<String> getAiAssist(@PathVariable Long userId) {
        String analysis = aiResumeService.getCandidateAnalysis(userId);
        return Result.success("分析成功", analysis);
    }
}
