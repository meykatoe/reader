package com.reader.limit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

// 滑動視窗
@Component
public class RateLimiter {

    // 限制記憶體
    private static final int MAX_KEYS = 20_000;

    private final Clock clock;
    private final Map<String, Window> windows = new LinkedHashMap<>();

    public RateLimiter(Clock clock) {
        this.clock = clock;
    }

    private record Window(Duration length, Deque<Instant> hits) {

        void expire(Instant now) {
            while (!hits.isEmpty() && !hits.peekFirst().plus(length).isAfter(now)) {
                hits.pollFirst();
            }
        }
    }

    // 先佔名額
    public synchronized boolean tryAcquire(String key, int limit, Duration length) {
        Instant now = clock.instant();
        Window window = windows.get(key);
        if (window == null) {
            makeRoom(now);
            window = new Window(length, new ArrayDeque<>());
            windows.put(key, window);
        }
        window.expire(now);
        if (window.hits().size() >= limit) {
            return false;
        }
        window.hits().addLast(now);
        return true;
    }

    // 退回最後一次
    public synchronized void release(String key) {
        Window window = windows.get(key);
        if (window != null) {
            window.hits().pollLast();
        }
    }

    public synchronized void reset(String key) {
        windows.remove(key);
    }

    private void makeRoom(Instant now) {
        if (windows.size() < MAX_KEYS) {
            return;
        }
        windows.values().removeIf(window -> {
            window.expire(now);
            return window.hits().isEmpty();
        });
        // 仍滿則汰舊
        Iterator<String> oldest = windows.keySet().iterator();
        while (windows.size() >= MAX_KEYS && oldest.hasNext()) {
            oldest.next();
            oldest.remove();
        }
    }
}
