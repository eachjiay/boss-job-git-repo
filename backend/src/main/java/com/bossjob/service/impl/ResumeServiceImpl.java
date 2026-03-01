package com.bossjob.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bossjob.common.PageResult;
import com.bossjob.dto.CandidateSearchDTO;
import com.bossjob.entity.Resume;
import com.bossjob.entity.User;
import com.bossjob.mapper.ResumeMapper;
import com.bossjob.service.ResumeService;
import com.bossjob.service.UserService;
import com.bossjob.vo.ResumeVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    public Resume getMyResume() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        return getOne(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, currentUser.getId()));
    }

    @Override
    public Resume saveOrUpdateResume(Resume resume) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        Resume existing = getOne(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, currentUser.getId()));

        if (existing != null) {
            resume.setId(existing.getId());
            resume.setUserId(currentUser.getId());
            resume.setUpdateTime(LocalDateTime.now());
            updateById(resume);
        } else {
            resume.setUserId(currentUser.getId());
            resume.setCreateTime(LocalDateTime.now());
            resume.setUpdateTime(LocalDateTime.now());
            save(resume);
        }

        return resume;
    }

    @Override
    public ResumeVO getResumeDetail(Long id) {
        Resume resume = getById(id);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        return convertToVO(resume);
    }

    @Override
    public Resume getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, userId));
    }

    @Override
    public PageResult<ResumeVO> searchCandidates(CandidateSearchDTO searchDTO) {
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            wrapper.and(w -> w
                    .like(Resume::getRealName, searchDTO.getKeyword())
                    .or()
                    .like(Resume::getSkills, searchDTO.getKeyword())
                    .or()
                    .like(Resume::getExpectPosition, searchDTO.getKeyword()));
        }

        // 城市筛选
        if (StringUtils.hasText(searchDTO.getCity())) {
            wrapper.eq(Resume::getExpectCity, searchDTO.getCity());
        }

        // 学历筛选
        if (StringUtils.hasText(searchDTO.getEducation())) {
            wrapper.eq(Resume::getEducation, searchDTO.getEducation());
        }

        // 工作年限
        if (StringUtils.hasText(searchDTO.getWorkYears())) {
            wrapper.eq(Resume::getWorkYears, searchDTO.getWorkYears());
        }

        // 年龄范围
        if (searchDTO.getAgeMin() != null) {
            wrapper.ge(Resume::getAge, searchDTO.getAgeMin());
        }
        if (searchDTO.getAgeMax() != null) {
            wrapper.le(Resume::getAge, searchDTO.getAgeMax());
        }

        // 求职状态
        if (StringUtils.hasText(searchDTO.getJobStatus())) {
            wrapper.eq(Resume::getJobStatus, searchDTO.getJobStatus());
        }

        // 期望薪资范围
        if (searchDTO.getSalaryMin() != null) {
            wrapper.ge(Resume::getExpectSalaryMax, searchDTO.getSalaryMin());
        }
        if (searchDTO.getSalaryMax() != null) {
            wrapper.le(Resume::getExpectSalaryMin, searchDTO.getSalaryMax());
        }

        wrapper.orderByDesc(Resume::getUpdateTime);

        Page<Resume> page = page(
                new Page<>(searchDTO.getPage(), searchDTO.getSize()),
                wrapper);

        List<ResumeVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList, page.getCurrent(), page.getSize());
    }

    private ResumeVO convertToVO(Resume resume) {
        ResumeVO vo = new ResumeVO();
        BeanUtils.copyProperties(resume, vo);
        vo.setSkills(parseJsonArray(resume.getSkills()));
        return vo;
    }

    private String[] parseJsonArray(String json) {
        if (!StringUtils.hasText(json)) {
            return new String[0];
        }
        try {
            return objectMapper.readValue(json, String[].class);
        } catch (JsonProcessingException e) {
            return new String[0];
        }
    }
}
