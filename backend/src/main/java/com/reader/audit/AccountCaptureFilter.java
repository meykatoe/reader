package com.reader.audit;

import com.reader.admin.auth.AdminPrincipal;
import com.reader.user.AuthUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// 登出前先記
public class AccountCaptureFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        Object principal = current == null ? null : current.getPrincipal();
        if (principal instanceof AuthUser user) {
            LogAccount.mark(request, user.id(), user.username());
        } else if (principal instanceof AdminPrincipal admin) {
            LogAccount.mark(request, admin.id(), admin.username());
        }
        chain.doFilter(request, response);
    }
}
