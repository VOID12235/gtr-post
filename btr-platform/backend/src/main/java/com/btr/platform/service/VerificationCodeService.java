package com.btr.platform.service;

import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationCodeService {

    private static final long TTL_MS = 10 * 60 * 1000L;
    private static final long RESEND_MS = 60 * 1000L;

    private record Entry(String code, long expiresAt, int attempts, long sentAt) {
    }

    private final Map<String, Entry> codes = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String issue(String purpose, String email) {
        String key = key(purpose, email);
        long now = System.currentTimeMillis();
        Entry existing = codes.get(key);
        if (existing != null && now - existing.sentAt() < RESEND_MS) {
            throw new BizException(ErrorCode.BAD_REQUEST, "发送过于频繁，请 60 秒后重试");
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        codes.put(key, new Entry(code, now + TTL_MS, 0, now));
        return code;
    }

    public void verify(String purpose, String email, String code) {
        String key = key(purpose, email);
        long now = System.currentTimeMillis();
        Entry entry = codes.get(key);
        if (entry == null || entry.expiresAt() < now) {
            codes.remove(key);
            throw new BizException(ErrorCode.BAD_REQUEST, "验证码已过期，请重新获取");
        }
        if (code == null || !entry.code().equals(code.trim())) {
            int attempts = entry.attempts() + 1;
            if (attempts >= 5) {
                codes.remove(key);
                throw new BizException(ErrorCode.BAD_REQUEST, "验证码错误次数过多，请重新获取");
            }
            codes.put(key, new Entry(entry.code(), entry.expiresAt(), attempts, entry.sentAt()));
            throw new BizException(ErrorCode.BAD_REQUEST, "验证码错误");
        }
        codes.remove(key);
    }

    public void remove(String purpose, String email) {
        codes.remove(key(purpose, email));
    }

    private String key(String purpose, String email) {
        return purpose + ":" + email;
    }
}
