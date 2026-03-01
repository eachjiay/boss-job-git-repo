package com.bossjob.service;

import com.bossjob.entity.Resume;

/**
 * AI 简历解析服务接口
 */
public interface AiResumeService {

    /**
     * 解析附件简历并返回结构化数据
     * 
     * @param attachmentUrl 简历附件地址
     * @return 解析后的简历数据
     */
    Resume parseResumeAttachment(String attachmentUrl);

    /**
     * 从文本中提取简历信息
     * 
     * @param resumeText 简历文本内容
     * @return 解析后的简历数据
     */
    Resume parseResumeText(String resumeText);

    /**
     * 获取求职者核心评价与面试建议
     * 
     * @param userId 用户ID
     * @return 包含评价和问题的JSON字符串
     */
    String getCandidateAnalysis(Long userId);

    /**
     * 计算职位与简历的匹配度
     * 
     * @param jobId    职位ID
     * @param resumeId 简历ID
     * @return 包含百分比分值和理由的JSON
     */
    String calculateMatchScore(Long jobId, Long resumeId);
}
