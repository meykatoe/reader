package com.reader.parser;

import com.reader.common.ApiException;
import com.reader.common.ErrorCode;

public class ParseException extends ApiException {

    public ParseException(ErrorCode code) {
        super(code);
    }
}
