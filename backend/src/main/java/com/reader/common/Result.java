package com.reader.common;

import org.slf4j.MDC;

// 統一回應格式
public record Result<T>(int code, String message, T data, String requestId) {

    // 成功固定為零
    public static final int SUCCESS = 0;

    public static <T> Result<T> ok(T data) {
        return new Result<>(SUCCESS, "OK", data, currentRequestId());
    }

    public static Result<Void> ok() {
        return ok(null);
    }

    public static <T> Result<T> error(ErrorCode code, T data) {
        return new Result<>(code.code(), code.message(), data, currentRequestId());
    }

    private static String currentRequestId() {
        return MDC.get(RequestIdFilter.KEY);
    }
}
