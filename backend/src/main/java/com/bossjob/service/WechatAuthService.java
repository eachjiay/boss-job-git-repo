package com.bossjob.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class WechatAuthService {

    /**
     * 模拟使用微信返回的 code 换取 OpenId
     * 
     * @param code 微信回调或前端传来的模拟 code
     * @return 模拟的微信 OpenId
     */
    public String getOpenIdByCode(String code) {
        log.info("[BossJob] Simulating WeChat getOpenId with code: {}", code);

        // 验证 code 是否为空，实际中应该向 https://api.weixin.qq.com/sns/oauth2/access_token 发送请求
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("WeChat code is empty");
        }

        // 我们通过 code 生成一个固定的模拟 OpenId (如果 code 相同，生成的 OpenId 也相同)
        // 这样可以保证测试时同一个"扫码"产生同一个用户
        if (code.startsWith("sim_wxc_")) {
            return "sim_wxo_" + code.substring(8);
        }

        // 降级使用 UUID 模拟真实的、随机的微信 openid
        return "sim_wxo_" + UUID.randomUUID().toString().substring(0, 16);
    }
}
