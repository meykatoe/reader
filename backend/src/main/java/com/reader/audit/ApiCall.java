package com.reader.audit;

import java.time.Instant;

// 單次呼叫
public record ApiCall(
        String requestId,
        Long accountId,
        String account,
        String method,
        String path,
        int httpStatus,
        int resultCode,
        String resultMessage,
        String errorDetail,
        String ip,
        long durationMs,
        Instant createdAt) {
}
