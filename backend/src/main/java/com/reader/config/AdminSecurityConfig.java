package com.reader.config;

import com.reader.admin.auth.AdminAuthority;
import com.reader.admin.auth.AdminRefreshFilter;
import com.reader.admin.auth.AdminRepository;
import com.reader.admin.auth.AdminSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
public class AdminSecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain adminFilterChain(HttpSecurity http,
                                         AdminSession adminSession,
                                         AdminRepository adminRepository) throws Exception {
        return http
                .securityMatcher("/api/admin/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/auth/login").permitAll()
                        .requestMatchers("/api/admin/auth/**").authenticated()
                        .requestMatchers("/api/admin/books/**")
                        .hasAuthority(AdminAuthority.BOOK_MANAGE.name())
                        .requestMatchers(HttpMethod.GET, "/api/admin/users")
                        .hasAuthority(AdminAuthority.USER_VIEW.name())
                        .requestMatchers(HttpMethod.PUT, "/api/admin/users/*/membership")
                        .hasAuthority(AdminAuthority.USER_MEMBERSHIP.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/users/*")
                        .hasAuthority(AdminAuthority.USER_DELETE.name())
                        // 未列出即拒絕
                        .anyRequest().denyAll())
                .csrf(csrf -> csrf.spa())
                .securityContext(context -> context.securityContextRepository(adminSession.repository()))
                .addFilterAfter(new AdminRefreshFilter(adminRepository), SecurityContextHolderFilter.class)
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .build();
    }
}
