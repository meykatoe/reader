package com.reader.parser.mobi;

import com.reader.common.ErrorCode;
import com.reader.parser.ParseException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

record MobiHeader(
        int compression,
        long textLength,
        int textRecords,
        Charset charset,
        String title,
        String author,
        int firstImageRecord,
        Integer coverOffset,
        int huffRecord,
        int huffCount,
        int trailingFlags) {

    private static final int EXTH_AUTHOR = 100;
    private static final int EXTH_COVER = 201;
    private static final int EXTH_TITLE = 503;

    static MobiHeader parse(byte[] record) {
        if (record.length < 132 || !Bytes.startsWith(record, 16, "MOBI")) {
            throw new ParseException(ErrorCode.BOOK_STRUCTURE_INVALID);
        }
        if (Bytes.u16(record, 12) != 0) {
            throw new ParseException(ErrorCode.BOOK_DRM_PROTECTED);
        }
        int headerLength = Bytes.index(record, 20);
        Charset charset = Bytes.u32(record, 28) == 65001
                ? StandardCharsets.UTF_8
                : Charset.forName("windows-1252");
        Map<Integer, byte[]> exth = readExth(record, 16 + headerLength);

        String title = text(exth.get(EXTH_TITLE), charset);
        if (title == null) {
            title = fullName(record, charset);
        }

        byte[] cover = exth.get(EXTH_COVER);
        return new MobiHeader(
                Bytes.u16(record, 0),
                Bytes.u32(record, 4),
                Bytes.u16(record, 8),
                charset,
                title,
                text(exth.get(EXTH_AUTHOR), charset),
                (int) Bytes.u32(record, 108),
                cover != null && cover.length == 4 ? (int) Bytes.u32(cover, 0) : null,
                (int) Bytes.u32(record, 112),
                (int) Bytes.u32(record, 116),
                headerLength >= 228 && record.length >= 244 ? Bytes.u16(record, 242) : 0);
    }

    private static String fullName(byte[] record, Charset charset) {
        long offset = Bytes.u32(record, 84);
        long length = Bytes.u32(record, 88);
        if (length == 0 || offset + length > record.length) {
            return null;
        }
        return new String(record, (int) offset, (int) length, charset);
    }

    // 延伸資訊
    private static Map<Integer, byte[]> readExth(byte[] record, int start) {
        Map<Integer, byte[]> values = new HashMap<>();
        boolean present = (Bytes.u32(record, 128) & 0x40) != 0;
        if (!present || !Bytes.startsWith(record, start, "EXTH") || start + 12 > record.length) {
            return values;
        }
        long count = Bytes.u32(record, start + 8);
        int position = start + 12;
        for (long i = 0; i < count && position + 8 <= record.length; i++) {
            int type = (int) Bytes.u32(record, position);
            long length = Bytes.u32(record, position + 4);
            if (length < 8 || position + length > record.length) {
                break;
            }
            byte[] value = new byte[(int) length - 8];
            System.arraycopy(record, position + 8, value, 0, value.length);
            values.putIfAbsent(type, value);
            position += (int) length;
        }
        return values;
    }

    private static String text(byte[] value, Charset charset) {
        return value == null ? null : new String(value, charset);
    }
}
