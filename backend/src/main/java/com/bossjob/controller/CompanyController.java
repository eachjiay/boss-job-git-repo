package com.bossjob.controller;

import com.bossjob.common.Result;
import com.bossjob.entity.Company;
import com.bossjob.service.CompanyService;
import com.bossjob.vo.CompanyDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/{id}")
    public Result<CompanyDetailVO> getCompanyDetail(@PathVariable Long id) {
        CompanyDetailVO detail = companyService.getCompanyDetail(id);
        return Result.success(detail);
    }

    @PostMapping
    public Result<Company> createCompany(@RequestBody Company company) {
        Company created = companyService.createCompany(company);
        return Result.success("创建成功", created);
    }

    @PutMapping("/{id}")
    public Result<Company> updateCompany(@PathVariable Long id, @RequestBody Company company) {
        company.setId(id);
        Company updated = companyService.updateCompany(company);
        return Result.success("更新成功", updated);
    }
}
