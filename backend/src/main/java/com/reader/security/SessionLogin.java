package com.reader.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

// 建立登入狀態
@Component
public class SessionLogin {

    private final SessionAuthenticationStrategy sessionStrategy =
            new ChangeSessionIdAuthenticationStrategy();

    public void establish(Authentication authentication,
                          SecurityContextRepository repository,
                          HttpServletRequest request,
                          HttpServletResponse response) {
        // 防固定攻擊
        sessionStrategy.onAuthentication(authentication, request, response);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        repository.saveContext(context, request, response);
    }
}
