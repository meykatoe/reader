package com.reader.limit;

import java.util.Locale;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

// 帳號防護
@Component
public class AccountGuard {

    private final RateLimiter limiter;
    private final LimitProperties limits;

    public AccountGuard(RateLimiter limiter, LimitProperties limits) {
        this.limiter = limiter;
        this.limits = limits;
    }

    // 成功才歸零
    public <T> T login(String scope, String username, String ip, Supplier<T> attempt) {
        String key = "login|" + scope + "|" + username.toLowerCase(Locale.ROOT) + "|" + ip;
        if (!limiter.tryAcquire(key, limits.loginAttempts(), limits.loginWindow())) {
            throw new TooManyRequestsException("登入嘗試次數過多，請稍後再試");
        }
        T result = attempt.get();
        limiter.reset(key);
        return result;
    }

    // 失敗不佔名額
    public void register(String ip, Runnable action) {
        String key = "register|" + ip;
        if (!limiter.tryAcquire(key, limits.registrations(), limits.registrationWindow())) {
            throw new TooManyRequestsException("此網路註冊次數過多，請稍後再試");
        }
        try {
            action.run();
        } catch (RuntimeException ex) {
            limiter.release(key);
            throw ex;
        }
    }
}
