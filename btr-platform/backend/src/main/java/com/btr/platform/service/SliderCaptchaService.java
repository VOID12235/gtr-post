package com.btr.platform.service;

import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SliderCaptchaService {

    private static final long CHALLENGE_TTL_MS = 2 * 60 * 1000L;
    private static final long TICKET_TTL_MS = 5 * 60 * 1000L;

    private record Challenge(long createdAt, String ticket) {
    }

    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();
    private final Map<String, Long> tickets = new ConcurrentHashMap<>();

    public Map<String, String> create() {
        cleanup();
        String challengeId = UUID.randomUUID().toString();
        challenges.put(challengeId, new Challenge(System.currentTimeMillis(), null));
        return Map.of("challengeId", challengeId);
    }

    public String verify(String challengeId, double position, long duration) {
        cleanup();
        Challenge challenge = challengeId == null ? null : challenges.get(challengeId);
        if (challenge == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "验证已过期，请重新滑动");
        }
        if (System.currentTimeMillis() - challenge.createdAt() > CHALLENGE_TTL_MS) {
            challenges.remove(challengeId);
            throw new BizException(ErrorCode.BAD_REQUEST, "验证已过期，请重新滑动");
        }
        if (position < 0.9) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请拖动到最右侧");
        }
        if (duration < 300 || duration > 20000) {
            throw new BizException(ErrorCode.BAD_REQUEST, "滑动验证未通过");
        }
        String ticket = UUID.randomUUID().toString();
        challenges.put(challengeId, new Challenge(challenge.createdAt(), ticket));
        tickets.put(ticket, System.currentTimeMillis() + TICKET_TTL_MS);
        return ticket;
    }

    public boolean consume(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            return false;
        }
        Long expiresAt = tickets.remove(ticket);
        return expiresAt != null && expiresAt >= System.currentTimeMillis();
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        challenges.entrySet().removeIf(entry -> now - entry.getValue().createdAt() > CHALLENGE_TTL_MS);
        tickets.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
