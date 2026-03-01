package com.bossjob.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bossjob.common.PageResult;
import com.bossjob.dto.JobSearchDTO;
import com.bossjob.entity.Job;
import com.bossjob.vo.JobDetailVO;
import com.bossjob.vo.JobListVO;

public interface JobService extends IService<Job> {

    /**
     * 搜索职位列表
     */
    PageResult<JobListVO> searchJobs(JobSearchDTO searchDTO);

    /**
     * 获取职位详情
     */
    JobDetailVO getJobDetail(Long id);

    /**
     * 发布职位
     */
    Job publishJob(Job job);

    /**
     * 更新职位
     */
    Job updateJob(Job job);

    /**
     * 下线职位
     */
    void offlineJob(Long id);

    /**
     * 获取热门职位
     */
    PageResult<JobListVO> getHotJobs(Integer page, Integer size);

    /**
     * 获取公司的职位列表
     */
    PageResult<JobListVO> getJobsByCompany(Long companyId, Integer page, Integer size);
}
