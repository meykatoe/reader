package com.reader.common;

import java.util.Map;

public class ApiException extends RuntimeException {

    private final ErrorCode code;
    private final transient Map<String, Object> data;

    public ApiException(ErrorCode code) {
        this(code, Map.of(), null);
    }

    // 附帶畫面資料
    public ApiException(ErrorCode code, Map<String, Object> data) {
        this(code, data, null);
    }

    protected ApiException(ErrorCode code, Map<String, Object> data, Throwable cause) {
        super(code.message(), cause);
        this.code = code;
        this.data = data;
    }

    public ErrorCode code() {
        return code;
    }

    public Map<String, Object> data() {
        return data;
    }
}
