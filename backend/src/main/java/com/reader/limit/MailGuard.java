package com.reader.limit;

import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import java.time.Duration;
import org.springframework.stereotype.Component;

// 寄信限流
@Component
public class MailGuard {

    private static final Duration HOUR = Duration.ofHours(1);
    private static final Duration DAY = Duration.ofDays(1);
    private static final String TOTAL = "mail-total";

    private final RateLimiter limiter;
    private final LimitProperties limits;

    public MailGuard(RateLimiter limiter, LimitProperties limits) {
        this.limiter = limiter;
        this.limits = limits;
    }

    // 依序佔用名額
    public void request(String email, String ip) {
        acquire("mail-ip|" + ip, limits.mailsPerIp(), HOUR, ErrorCode.MAIL_RATE_LIMITED);
        acquire("mail-cooldown|" + email, 1, limits.mailCooldown(),
                ErrorCode.MAIL_RATE_LIMITED);
        acquire("mail-address|" + email, limits.mailsPerAddress(), HOUR,
                ErrorCode.MAIL_RATE_LIMITED);
    }

    // 寄失敗可重試
    public void release(String email) {
        limiter.release("mail-cooldown|" + email);
    }

    // 保護配額
    public void reserveDelivery() {
        acquire(TOTAL, limits.mailsPerDay(), DAY, ErrorCode.MAIL_RATE_LIMITED);
    }

    public void verification(String ip) {
        acquire("verify|" + ip, limits.verificationsPerIp(), limits.loginWindow(),
                ErrorCode.VERIFY_LIMITED);
    }

    private void acquire(String key, int limit, Duration window, ErrorCode code) {
        if (!limiter.tryAcquire(key, limit, window)) {
            throw new ApiException(code);
        }
    }
}
