package com.reader.parser;

import com.reader.common.ApiException;
import org.springframework.http.HttpStatus;

public class ParseException extends ApiException {

    public ParseException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
