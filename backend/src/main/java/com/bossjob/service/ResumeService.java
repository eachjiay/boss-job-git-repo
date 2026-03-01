package com.bossjob.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bossjob.common.PageResult;
import com.bossjob.dto.CandidateSearchDTO;
import com.bossjob.entity.Resume;
import com.bossjob.vo.ResumeVO;

public interface ResumeService extends IService<Resume> {

    /**
     * 获取当前用户的简历
     */
    Resume getMyResume();

    /**
     * 创建或更新简历
     */
    Resume saveOrUpdateResume(Resume resume);

    /**
     * 获取简历详情
     */
    ResumeVO getResumeDetail(Long id);

    /**
     * 根据用户ID获取简历
     */
    Resume getByUserId(Long userId);

    /**
     * 搜索候选人 (HR端)
     */
    PageResult<ResumeVO> searchCandidates(CandidateSearchDTO searchDTO);
}
