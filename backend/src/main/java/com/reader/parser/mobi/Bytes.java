package com.reader.parser.mobi;

import com.reader.parser.ParseException;

// 大端序讀取
final class Bytes {

    private Bytes() {
    }

    static int u8(byte[] data, int offset) {
        return data[offset] & 0xFF;
    }

    static int u16(byte[] data, int offset) {
        return u8(data, offset) << 8 | u8(data, offset + 1);
    }

    static long u32(byte[] data, int offset) {
        return (long) u16(data, offset) << 16 | u16(data, offset + 2);
    }

    static int index(byte[] data, int offset) {
        long value = u32(data, offset);
        if (value > Integer.MAX_VALUE) {
            throw new ParseException("MOBI 結構損毀");
        }
        return (int) value;
    }

    static boolean startsWith(byte[] data, int offset, String magic) {
        if (offset < 0 || data.length < offset + magic.length()) {
            return false;
        }
        for (int i = 0; i < magic.length(); i++) {
            if (data[offset + i] != (byte) magic.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}
