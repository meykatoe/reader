package com.reader.limit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

    private static final Duration MINUTE = Duration.ofMinutes(1);

    private Instant now = Instant.parse("2027-01-01T00:00:00Z");

    private final RateLimiter limiter = new RateLimiter(new Clock() {
        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    });

    @Test
    void recoversAfterWindow() {
        assertThat(limiter.tryAcquire("k", 2, MINUTE)).isTrue();
        assertThat(limiter.tryAcquire("k", 2, MINUTE)).isTrue();
        assertThat(limiter.tryAcquire("k", 2, MINUTE)).isFalse();

        now = now.plus(MINUTE);

        assertThat(limiter.tryAcquire("k", 2, MINUTE)).isTrue();
    }

    @Test
    void holdsUnderConcurrency() {
        try (var executor = Executors.newFixedThreadPool(16)) {
            long granted = IntStream.range(0, 500)
                    .mapToObj(i -> executor.submit(() -> limiter.tryAcquire("k", 5, MINUTE)))
                    .toList().stream()
                    .filter(future -> {
                        try {
                            return future.get();
                        } catch (Exception ex) {
                            throw new IllegalStateException(ex);
                        }
                    })
                    .count();
            assertThat(granted).isEqualTo(5);
        }
    }
}
