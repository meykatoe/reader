package com.reader.audit;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ApiLogWriter {

    private static final Logger log = LoggerFactory.getLogger(ApiLogWriter.class);

    private static final int QUEUE_CAPACITY = 5000;
    private static final int BATCH_SIZE = 200;

    private final UserApiLogRepository userLogs;
    private final AdminApiLogRepository adminLogs;
    private final BlockingQueue<ApiCall> pending = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final AtomicLong dropped = new AtomicLong();

    public ApiLogWriter(UserApiLogRepository userLogs, AdminApiLogRepository adminLogs) {
        this.userLogs = userLogs;
        this.adminLogs = adminLogs;
    }

    // 後臺即時寫入
    void writeAdmin(ApiCall call) {
        try {
            adminLogs.save(new AdminApiLog(call));
        } catch (RuntimeException ex) {
            log.error("Failed to save admin log: {}", call, ex);
        }
    }

    // 官網排隊批次
    void queueUser(ApiCall call) {
        if (!pending.offer(call)) {
            dropped.incrementAndGet();
        }
    }

    @Scheduled(fixedDelay = 1000)
    @PreDestroy
    public synchronized void flush() {
        long lost = dropped.getAndSet(0);
        if (lost > 0) {
            log.warn("Log queue full, dropped {} user log entries", lost);
        }
        List<ApiCall> batch = new ArrayList<>(BATCH_SIZE);
        while (pending.drainTo(batch, BATCH_SIZE) > 0) {
            try {
                userLogs.saveAll(batch.stream().map(UserApiLog::new).toList());
            } catch (RuntimeException ex) {
                log.error("Failed to save {} user log entries", batch.size(), ex);
            }
            batch.clear();
        }
    }
}
