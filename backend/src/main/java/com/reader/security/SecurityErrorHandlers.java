package com.reader.security;

import com.reader.common.ErrorCode;
import com.reader.common.ResultWriter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

// 安全層回應
@Component
public class SecurityErrorHandlers {

    private final ResultWriter writer;

    public SecurityErrorHandlers(ResultWriter writer) {
        this.writer = writer;
    }

    public AuthenticationEntryPoint unauthenticated() {
        return (request, response, ex) ->
                writer.error(request, response, ErrorCode.AUTH_REQUIRED);
    }

    public AccessDeniedHandler denied() {
        return (request, response, ex) -> writer.error(request, response,
                ex instanceof CsrfException ? ErrorCode.CSRF_INVALID : ErrorCode.ACCESS_DENIED);
    }
}
