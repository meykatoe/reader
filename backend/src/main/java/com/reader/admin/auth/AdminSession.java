package com.reader.admin.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

// 獨立登入狀態
@Component
public class AdminSession {

    private static final String KEY = "ADMIN_SECURITY_CONTEXT";

    private final HttpSessionSecurityContextRepository repository =
            new HttpSessionSecurityContextRepository();

    public AdminSession() {
        repository.setSpringSecurityContextKey(KEY);
    }

    public SecurityContextRepository repository() {
        return repository;
    }

    // 保留用戶登入
    void end(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(KEY);
            request.changeSessionId();
        }
        SecurityContextHolder.clearContext();
    }
}
