package com.bossjob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:bossjob@example.com}")
    private String fromEmail;

    // 存储验证码，真实项目应使用Redis
    private final Map<String, String> emailCodeMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void sendCode(String email) {
        // 生成6位随机验证码
        String code = generateCode();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【BOSS直聘】登录验证码");
            message.setText("您的登录验证码是：" + code + "，5分钟内有效。请勿泄露给他人。");
            
            mailSender.send(message);
            
            // 存入内存
            emailCodeMap.put(email, code);

            // 5分钟后过期
            scheduler.schedule(() -> emailCodeMap.remove(email), 5, TimeUnit.MINUTES);

            log.info("[BossJob] Email verification code sent to {}: {} (valid for 5 min)", email, code);
        } catch (Exception e) {
            log.error("Failed to send email to {}", email, e);
            throw new RuntimeException("发送邮件验证码失败，请重试");
        }
    }

    public boolean verifyCode(String email, String code) {
        String storedCode = emailCodeMap.get(email);
        if (storedCode != null && storedCode.equals(code)) {
            // 验证成功后移除
            emailCodeMap.remove(email);
            return true;
        }
        return false;
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}