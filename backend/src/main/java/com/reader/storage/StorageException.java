package com.reader.storage;

import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import java.util.Map;

public class StorageException extends ApiException {

    public StorageException(Throwable cause) {
        super(ErrorCode.STORAGE_UNAVAILABLE, Map.of(), cause);
    }
}
