package com.reader.user;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

// 避免時間差
@Component
class BackgroundMailer {

    private static final Logger log = LoggerFactory.getLogger(BackgroundMailer.class);
    private static final int QUEUE_CAPACITY = 100;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(QUEUE_CAPACITY));

    void submit(Runnable task) {
        // 沿用編號
        Map<String, String> context = MDC.getCopyOfContextMap();
        try {
            executor.execute(() -> run(task, context));
        } catch (RejectedExecutionException ex) {
            log.error("Mail queue is full, message dropped", ex);
        }
    }

    @PreDestroy
    void shutdown() throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
            log.warn("Unsent mail dropped during shutdown");
        }
    }

    private static void run(Runnable task, Map<String, String> context) {
        if (context != null) {
            MDC.setContextMap(context);
        }
        try {
            task.run();
        } catch (RuntimeException ex) {
            log.error("Failed to send mail in background", ex);
        } finally {
            MDC.clear();
        }
    }
}
