package com.reader.storage;

import com.reader.common.ApiException;
import org.springframework.http.HttpStatus;

public class StorageException extends ApiException {

    public StorageException(Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "儲存服務暫時無法使用");
        initCause(cause);
    }
}
