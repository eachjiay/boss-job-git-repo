package com.bossjob.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bossjob.common.PageResult;
import com.bossjob.entity.Application;
import com.bossjob.vo.ApplicationVO;

public interface ApplicationService extends IService<Application> {

    /**
     * 投递简历
     */
    Application apply(Long jobId);

    /**
     * 获取我的投递记录 (求职者)
     */
    PageResult<ApplicationVO> getMyApplications(Integer page, Integer size);

    /**
     * 获取收到的简历 (HR端)
     */
    PageResult<ApplicationVO> getReceivedApplications(String status, Integer page, Integer size);

    /**
     * 更新申请状态
     */
    Application updateStatus(Long id, String status);

    /**
     * 检查是否已投递
     */
    boolean hasApplied(Long jobId);
}
