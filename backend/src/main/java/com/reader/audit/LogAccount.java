package com.reader.audit;

import jakarta.servlet.http.HttpServletRequest;

// 本次操作帳號
public record LogAccount(Long id, String name) {

    private static final String KEY = LogAccount.class.getName();

    public static void mark(HttpServletRequest request, Long id, String name) {
        request.setAttribute(KEY, new LogAccount(id, name));
    }

    static LogAccount of(HttpServletRequest request) {
        return request.getAttribute(KEY) instanceof LogAccount account
                ? account
                : new LogAccount(null, null);
    }
}
