package com.bossjob.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bossjob.common.Result;
import com.bossjob.service.FavoriteService;
import com.bossjob.vo.JobDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 职位收藏控制器
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 收藏职位
     */
    @PostMapping("/{jobId}")
    public Result<Void> addFavorite(@PathVariable Long jobId) {
        favoriteService.addFavorite(jobId);
        return Result.success(null);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{jobId}")
    public Result<Void> removeFavorite(@PathVariable Long jobId) {
        favoriteService.removeFavorite(jobId);
        return Result.success(null);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check/{jobId}")
    public Result<Boolean> checkFavorite(@PathVariable Long jobId) {
        return Result.success(favoriteService.isFavorited(jobId));
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/my")
    public Result<Page<JobDetailVO>> getMyFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(favoriteService.getMyFavorites(page, size));
    }
}
