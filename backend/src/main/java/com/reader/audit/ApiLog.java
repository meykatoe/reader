package com.reader.audit;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;

// 兩表共用欄位
@MappedSuperclass
public abstract class ApiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, length = 16)
    private String requestId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(length = 64)
    private String account;

    @Column(nullable = false, length = 8)
    private String method;

    @Column(nullable = false)
    private String path;

    @Column(name = "http_status", nullable = false)
    private int httpStatus;

    @Column(name = "result_code", nullable = false)
    private int resultCode;

    @Column(name = "result_message", nullable = false)
    private String resultMessage;

    @Column(name = "error_detail", length = 1000)
    private String errorDetail;

    @Column(nullable = false, length = 45)
    private String ip;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ApiLog() {
    }

    protected ApiLog(ApiCall call) {
        this.requestId = truncate(call.requestId(), 16);
        this.accountId = call.accountId();
        this.account = truncate(call.account(), 64);
        this.method = truncate(call.method(), 8);
        this.path = truncate(call.path(), 255);
        this.httpStatus = call.httpStatus();
        this.resultCode = call.resultCode();
        this.resultMessage = truncate(call.resultMessage(), 255);
        this.errorDetail = truncate(call.errorDetail(), 1000);
        this.ip = truncate(call.ip(), 45);
        this.durationMs = call.durationMs();
        this.createdAt = call.createdAt();
    }

    private static String truncate(String value, int length) {
        return value == null || value.length() <= length ? value : value.substring(0, length);
    }

    public String getRequestId() {
        return requestId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccount() {
        return account;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public String getIp() {
        return ip;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
