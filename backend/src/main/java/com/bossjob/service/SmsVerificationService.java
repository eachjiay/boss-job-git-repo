package com.bossjob.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsVerificationService {

    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;

    public String sendCode(String phone) {
        CodeEntry existing = codeStore.get(phone);
        if (existing != null && existing.sendTime.plusSeconds(60).isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Verification code sent too frequently, please wait 60 seconds");
        }
        String code = generateCode();
        codeStore.put(phone, new CodeEntry(code, LocalDateTime.now()));
        System.out.println("[BossJob] Code sent to " + phone + ": " + code + " (valid for " + CODE_EXPIRE_MINUTES + " min)");
        return code;
    }

    public boolean verifyCode(String phone, String code) {
        CodeEntry entry = codeStore.get(phone);
        if (entry == null) return false;
        if (entry.sendTime.plusMinutes(CODE_EXPIRE_MINUTES).isBefore(LocalDateTime.now())) {
            codeStore.remove(phone);
            return false;
        }
        boolean matched = entry.code.equals(code);
        if (matched) codeStore.remove(phone);
        return matched;
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    private static class CodeEntry {
        final String code;
        final LocalDateTime sendTime;
        CodeEntry(String code, LocalDateTime sendTime) {
            this.code = code;
            this.sendTime = sendTime;
        }
    }
}