package com.reader.limit;

import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
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

    public void unlock(String scope, String username, String ip) {
        limiter.reset(loginKey(scope, username, ip));
    }

    private static String loginKey(String scope, String username, String ip) {
        return "login|" + scope + "|" + username.toLowerCase(Locale.ROOT) + "|" + ip;
    }

    // 成功才歸零
    public <T> T login(String scope, String username, String ip, Supplier<T> attempt) {
        String key = loginKey(scope, username, ip);
        if (!limiter.tryAcquire(key, limits.loginAttempts(), limits.loginWindow())) {
            throw new ApiException(ErrorCode.LOGIN_LOCKED);
        }
        T result = attempt.get();
        limiter.reset(key);
        return result;
    }

    // 失敗不佔名額
    public void register(String ip, Runnable action) {
        String key = "register|" + ip;
        if (!limiter.tryAcquire(key, limits.registrations(), limits.registrationWindow())) {
            throw new ApiException(ErrorCode.REGISTER_LIMITED);
        }
        try {
            action.run();
        } catch (RuntimeException ex) {
            limiter.release(key);
            throw ex;
        }
    }
}
