package com.reader.admin.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// 即時生效
public class AdminRefreshFilter extends OncePerRequestFilter {

    private final AdminRepository adminRepository;

    public AdminRefreshFilter(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        if (current != null && current.getPrincipal() instanceof AdminPrincipal principal) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            adminRepository.findById(principal.id())
                    // 改密碼即登出
                    .filter(admin -> admin.getPasswordHash().equals(principal.passwordHash()))
                    .flatMap(AdminPrincipal::from)
                    .ifPresent(fresh -> context.setAuthentication(
                            UsernamePasswordAuthenticationToken.authenticated(
                                    fresh, null, fresh.getAuthorities())));
            SecurityContextHolder.setContext(context);
        }
        chain.doFilter(request, response);
    }
}
