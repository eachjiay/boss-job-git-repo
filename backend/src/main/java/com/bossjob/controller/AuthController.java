package com.bossjob.controller;

import com.bossjob.common.Result;
import com.bossjob.dto.*;
import com.bossjob.entity.User;
import com.bossjob.service.EmailVerificationService;
import com.bossjob.service.SmsVerificationService;
import com.bossjob.service.UserService;
import com.bossjob.service.WechatAuthService;
import com.bossjob.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final SmsVerificationService smsVerificationService;
    private final EmailVerificationService emailVerificationService;
    private final WechatAuthService wechatAuthService;

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        User user = userService.register(registerDTO);
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success("注册成功", vo);
    }

    // --- 账号密码登录 ---
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        User user = userService.findByUsername(loginDTO.getUsername());
        return buildLoginResult(token, user);
    }

    @PostMapping("/send-code")
    public Result<String> sendCode(@Valid @RequestBody SendCodeDTO sendCodeDTO) {
        // 模拟发送短信，直接返回验证码（实际生产中不返回，仅返回发送成功）
        String code = smsVerificationService.sendCode(sendCodeDTO.getPhone());
        return Result.success("验证码发送成功（模拟环境直接返回）", code);
    }

    @PostMapping("/login/phone")
    public Result<Map<String, Object>> loginByPhone(@Valid @RequestBody PhoneLoginDTO phoneLoginDTO) {
        // 1. 校验验证码
        boolean isValid = smsVerificationService.verifyCode(phoneLoginDTO.getPhone(), phoneLoginDTO.getCode());
        if (!isValid) {
            return Result.error(400, "验证码错误或已过期");
        }

        // 2. 登录或注册
        String token = userService.loginByPhone(phoneLoginDTO.getPhone());
        User user = userService.findByPhone(phoneLoginDTO.getPhone());
        return buildLoginResult(token, user);
    }

    // --- 邮箱验证码登录流程 ---
    @PostMapping("/send-email-code")
    public Result<String> sendEmailCode(@Valid @RequestBody SendEmailCodeDTO sendEmailCodeDTO) {
        // 请求发送邮件
        emailVerificationService.sendCode(sendEmailCodeDTO.getEmail());
        return Result.success("验证码已发送至您的邮箱，请查收");
    }

    @PostMapping("/login/email")
    public Result<Map<String, Object>> loginByEmail(@Valid @RequestBody EmailLoginDTO loginDTO) {
        // 校验邮箱验证码
        if (!emailVerificationService.verifyCode(loginDTO.getEmail(), loginDTO.getCode())) {
            return Result.error(400, "验证码错误或已过期");
        }

        String token = userService.loginByEmail(loginDTO.getEmail());
        return buildLoginResult(token, userService.findByEmail(loginDTO.getEmail()));
    }

    // --- 微信扫码登录流程 (模拟) ---
    @PostMapping("/login/wechat")
    public Result<Map<String, Object>> loginByWechat(@Valid @RequestBody WechatLoginDTO loginDTO) {
        // 1. 模拟换取 openId
        String openId = wechatAuthService.getOpenIdByCode(loginDTO.getCode());

        // 2. 根据 openId 登录或注册
        String token = userService.loginByWechat(openId);
        User user = userService.findByWechatOpenId(openId);

        return buildLoginResult(token, user);
    }

    private Result<Map<String, Object>> buildLoginResult(String token, User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", vo);

        return Result.success("登录成功", data);
    }
}