package com.reader.parser.mobi;

import com.reader.common.ErrorCode;
import com.reader.parser.ParseException;
import java.util.Arrays;

// 容器格式
final class PalmDatabase {

    private static final int HEADER_SIZE = 78;

    private final byte[] data;
    private final int[] offsets;

    PalmDatabase(byte[] data) {
        if (data.length < HEADER_SIZE || !Bytes.startsWith(data, 60, "BOOKMOBI")) {
            throw new ParseException(ErrorCode.BOOK_STRUCTURE_INVALID);
        }
        this.data = data;
        this.offsets = new int[Bytes.u16(data, 76) + 1];
        for (int i = 0; i < offsets.length - 1; i++) {
            offsets[i] = Bytes.index(data, HEADER_SIZE + i * 8);
        }
        offsets[offsets.length - 1] = data.length;
    }

    int size() {
        return offsets.length - 1;
    }

    byte[] record(int index) {
        if (index < 0 || index >= size()
                || offsets[index] > offsets[index + 1] || offsets[index + 1] > data.length) {
            throw new ParseException(ErrorCode.BOOK_STRUCTURE_INVALID);
        }
        return Arrays.copyOfRange(data, offsets[index], offsets[index + 1]);
    }
}
