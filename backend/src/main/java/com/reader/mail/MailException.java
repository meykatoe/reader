package com.reader.mail;

import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import java.util.Map;

public class MailException extends ApiException {

    public MailException(Throwable cause) {
        super(ErrorCode.MAIL_UNAVAILABLE, Map.of(), cause);
    }
}
