package com.reader.quota;

import com.reader.book.Book;
import com.reader.limit.LimitProperties;
import com.reader.limit.RateLimiter;
import com.reader.limit.TooManyRequestsException;
import java.time.Duration;
import org.springframework.stereotype.Component;

// 閱讀防爬
@Component
public class ReadGuard {

    private final AccessQuotaService quotaService;
    private final RateLimiter limiter;
    private final LimitProperties limits;

    public ReadGuard(AccessQuotaService quotaService,
                     RateLimiter limiter,
                     LimitProperties limits) {
        this.quotaService = quotaService;
        this.limiter = limiter;
        this.limits = limits;
    }

    public void open(Book book, Long userId) {
        quotaService.grant(book, userId, AccessKind.READ);
    }

    public void readChapter(Book book, Long userId) {
        if (!book.isOwnedBy(userId)
                && !limiter.tryAcquire("chapter|" + userId,
                        limits.chaptersPerMinute(), Duration.ofMinutes(1))) {
            throw new TooManyRequestsException("翻閱速度過快，請稍後再試");
        }
        open(book, userId);
    }
}
