package com.reader.config;

import com.reader.audit.AccountCaptureFilter;
import com.reader.common.ResultWriter;
import com.reader.security.SecurityErrorHandlers;
import com.reader.user.StaleSessionFilter;
import com.reader.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

    @Bean
    @Order(2)
    SecurityFilterChain filterChain(HttpSecurity http,
                                    SecurityContextRepository contextRepository,
                                    UserRepository userRepository,
                                    SecurityErrorHandlers errors,
                                    ResultWriter resultWriter) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/register/code",
                                "/api/auth/login", "/api/auth/password/forgot",
                                "/api/auth/password/reset").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        // 其餘為靜態頁
                        .anyRequest().permitAll())
                .csrf(csrf -> csrf.spa())
                .addFilterAfter(new AccountCaptureFilter(), SecurityContextHolderFilter.class)
                .addFilterAfter(new StaleSessionFilter(userRepository), AccountCaptureFilter.class)
                .securityContext(context -> context.securityContextRepository(contextRepository))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(errors.unauthenticated())
                        .accessDeniedHandler(errors.denied()))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                resultWriter.success(response)))
                .build();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
