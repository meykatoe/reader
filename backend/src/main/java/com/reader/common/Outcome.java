package com.reader.common;

import jakarta.servlet.http.HttpServletRequest;

// 回應結果
public record Outcome(int code, String message, String detail) {

    private static final String KEY = Outcome.class.getName();

    public static void mark(HttpServletRequest request, ErrorCode code, Throwable cause) {
        // 僅系統錯誤
        String detail = cause != null && code.status().is5xxServerError() ? describe(cause) : null;
        request.setAttribute(KEY, new Outcome(code.code(), code.message(), detail));
    }

    public static Outcome of(HttpServletRequest request) {
        return request.getAttribute(KEY) instanceof Outcome outcome ? outcome : null;
    }

    private static String describe(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getClass().getName() + ": " + root.getMessage();
    }
}
