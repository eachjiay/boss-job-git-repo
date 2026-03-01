package com.bossjob.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bossjob.entity.Company;
import com.bossjob.vo.CompanyDetailVO;

public interface CompanyService extends IService<Company> {

    /**
     * 获取公司详情
     */
    CompanyDetailVO getCompanyDetail(Long id);

    /**
     * 创建公司
     */
    Company createCompany(Company company);

    /**
     * 更新公司信息
     */
    Company updateCompany(Company company);

    /**
     * 根据用户ID获取公司
     */
    Company getByUserId(Long userId);
}
