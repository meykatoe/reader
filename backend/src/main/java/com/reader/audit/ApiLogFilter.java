package com.reader.audit;

import com.reader.common.ErrorCode;
import com.reader.common.Outcome;
import com.reader.common.RequestIdFilter;
import com.reader.common.Result;
import com.reader.common.ResultWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// 包住安全層
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ApiLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLogFilter.class);

    private final ApiLogWriter writer;
    private final ResultWriter resultWriter;
    private final Clock clock;

    public ApiLogFilter(ApiLogWriter writer, ResultWriter resultWriter, Clock clock) {
        this.writer = writer;
        this.resultWriter = resultWriter;
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !path(request).startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Instant startedAt = clock.instant();
        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } catch (ServletException | RuntimeException ex) {
            // 過濾層例外
            log.error("Unhandled error on {} {}", request.getMethod(), path(request), ex);
            Outcome.mark(request, ErrorCode.INTERNAL_ERROR, ex);
            if (response.isCommitted()) {
                throw ex;
            }
            resultWriter.error(request, response, ErrorCode.INTERNAL_ERROR);
        } finally {
            long millis = (System.nanoTime() - start) / 1_000_000;
            record(request, response, startedAt, millis);
        }
    }

    private void record(HttpServletRequest request, HttpServletResponse response,
                        Instant startedAt, long millis) {
        LogAccount account = LogAccount.of(request);
        Outcome outcome = outcomeOf(request, response.getStatus());
        String path = path(request);
        // 不記請求內容
        ApiCall call = new ApiCall(
                (String) request.getAttribute(RequestIdFilter.KEY),
                account.id(), account.name(),
                request.getMethod(), path,
                response.getStatus(), outcome.code(), outcome.message(), outcome.detail(),
                request.getRemoteAddr(), millis, startedAt);
        if (path.startsWith("/api/admin/")) {
            writer.writeAdmin(call);
        } else {
            writer.queueUser(call);
        }
    }

    private static Outcome outcomeOf(HttpServletRequest request, int status) {
        Outcome marked = Outcome.of(request);
        if (marked != null) {
            return marked;
        }
        if (status < 400) {
            return new Outcome(Result.SUCCESS, "OK", null);
        }
        ErrorCode code = ErrorCode.forStatus(status);
        return new Outcome(code.code(), code.message(), null);
    }

    private static String path(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}
