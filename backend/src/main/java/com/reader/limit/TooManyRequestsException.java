package com.reader.limit;

import com.reader.common.ApiException;
import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends ApiException {

    public TooManyRequestsException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
