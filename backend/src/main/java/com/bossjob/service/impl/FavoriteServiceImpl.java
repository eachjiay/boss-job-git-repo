package com.bossjob.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bossjob.entity.Favorite;
import com.bossjob.entity.Job;
import com.bossjob.entity.User;
import com.bossjob.mapper.FavoriteMapper;
import com.bossjob.service.FavoriteService;
import com.bossjob.service.JobService;
import com.bossjob.service.UserService;
import com.bossjob.vo.JobDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private final UserService userService;
    @Lazy
    private final JobService jobService;

    @Override
    public void addFavorite(Long jobId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        // 检查职位是否存在
        Job job = jobService.getById(jobId);
        if (job == null) {
            throw new RuntimeException("职位不存在");
        }

        // 检查是否已收藏
        if (isFavorited(jobId)) {
            return; // 已收藏则直接返回
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(currentUser.getId());
        favorite.setJobId(jobId);
        favorite.setCreateTime(LocalDateTime.now());
        save(favorite);
    }

    @Override
    public void removeFavorite(Long jobId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        remove(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, currentUser.getId())
                .eq(Favorite::getJobId, jobId));
    }

    @Override
    public boolean isFavorited(Long jobId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return count(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, currentUser.getId())
                .eq(Favorite::getJobId, jobId)) > 0;
    }

    @Override
    public Page<JobDetailVO> getMyFavorites(int page, int size) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        // 获取收藏记录
        Page<Favorite> favoritePage = page(
                new Page<>(page, size),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, currentUser.getId())
                        .orderByDesc(Favorite::getCreateTime));

        // 获取职位详情
        List<Long> jobIds = favoritePage.getRecords().stream()
                .map(Favorite::getJobId)
                .collect(Collectors.toList());

        if (jobIds.isEmpty()) {
            Page<JobDetailVO> emptyPage = new Page<>(page, size, 0);
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }

        // 批量获取职位信息
        List<JobDetailVO> jobVOList = jobIds.stream()
                .map(jobId -> {
                    try {
                        return jobService.getJobDetail(jobId);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        Page<JobDetailVO> result = new Page<>(page, size, favoritePage.getTotal());
        result.setRecords(jobVOList);
        return result;
    }
}
