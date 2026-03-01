package com.bossjob.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bossjob.entity.Favorite;
import com.bossjob.vo.JobDetailVO;

public interface FavoriteService extends IService<Favorite> {

    /**
     * 收藏职位
     */
    void addFavorite(Long jobId);

    /**
     * 取消收藏
     */
    void removeFavorite(Long jobId);

    /**
     * 检查是否已收藏
     */
    boolean isFavorited(Long jobId);

    /**
     * 获取我的收藏列表
     */
    Page<JobDetailVO> getMyFavorites(int page, int size);
}
