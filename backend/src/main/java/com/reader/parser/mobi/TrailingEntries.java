package com.reader.parser.mobi;

import java.util.Arrays;

// 記錄尾端資料
final class TrailingEntries {

    private TrailingEntries() {
    }

    static byte[] strip(byte[] record, int flags) {
        int size = record.length;
        for (int bits = flags >> 1; bits != 0; bits >>= 1) {
            if ((bits & 1) != 0) {
                size -= entrySize(record, size);
            }
        }
        // 跨記錄字元
        if ((flags & 1) != 0 && size > 0) {
            size -= (record[size - 1] & 3) + 1;
        }
        return Arrays.copyOf(record, Math.max(size, 0));
    }

    // 反向變長整數
    private static int entrySize(byte[] record, int size) {
        int result = 0;
        int shift = 0;
        while (size > 0) {
            int value = record[--size] & 0xFF;
            result |= (value & 0x7F) << shift;
            shift += 7;
            if ((value & 0x80) != 0 || shift >= 28) {
                break;
            }
        }
        return result;
    }
}
