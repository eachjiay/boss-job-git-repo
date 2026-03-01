package com.bossjob.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bossjob.dto.LoginDTO;
import com.bossjob.dto.RegisterDTO;
import com.bossjob.entity.User;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    User register(RegisterDTO registerDTO);

    /**
     * 用户登录 (用户名+密码)
     */
    String login(LoginDTO loginDTO);

    /**
     * 手机号验证码登录
     */
    String loginByPhone(String phone);

    /**
     * 邮箱验证码登录
     */
    String loginByEmail(String email);

    /**
     * 微信扫码登录 (模拟)
     */
    String loginByWechat(String openId);

    /**
     * 根据用户名获取信息
     */
    User findByUsername(String username);

    /**
     * 根据手机号获取信息
     */
    User findByPhone(String phone);

    /**
     * 根据邮箱获取信息
     */
    User findByEmail(String email);

    /**
     * 根据微信 OpenId 获取信息
     */
    User findByWechatOpenId(String openId);

    /**
     * 获取当前登录用户信息
     */
    User getCurrentUser();

    /**
     * 兼容性方法 (保留)
     */
    User getByUsername(String username);
}