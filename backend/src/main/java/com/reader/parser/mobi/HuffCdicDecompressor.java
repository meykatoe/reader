package com.reader.parser.mobi;

import com.reader.common.ErrorCode;
import com.reader.parser.ParseException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

// 霍夫曼解壓
final class HuffCdicDecompressor implements Decompressor {

    private static final int MAX_DEPTH = 32;
    private static final int MAX_OUTPUT = 1024 * 1024;

    private final int[] cacheLength = new int[256];
    private final boolean[] cacheTerminal = new boolean[256];
    private final long[] cacheMax = new long[256];
    private final long[] minCode = new long[33];
    private final long[] maxCode = new long[33];
    private final List<Phrase> dictionary = new ArrayList<>();

    private static final class Phrase {

        private byte[] data;
        private boolean expanded;

        private Phrase(byte[] data, boolean expanded) {
            this.data = data;
            this.expanded = expanded;
        }
    }

    HuffCdicDecompressor(byte[] huff, List<byte[]> cdicRecords) {
        if (!Bytes.startsWith(huff, 0, "HUFF")) {
            throw new ParseException(ErrorCode.BOOK_STRUCTURE_INVALID);
        }
        loadTables(huff);
        cdicRecords.forEach(this::loadDictionary);
    }

    private void loadTables(byte[] huff) {
        int cacheOffset = Bytes.index(huff, 8);
        int baseOffset = Bytes.index(huff, 12);
        for (int i = 0; i < 256; i++) {
            long value = Bytes.u32(huff, cacheOffset + i * 4);
            int length = (int) (value & 0x1F);
            if (length == 0) {
                throw new ParseException(ErrorCode.BOOK_STRUCTURE_INVALID);
            }
            cacheLength[i] = length;
            cacheTerminal[i] = (value & 0x80) != 0;
            cacheMax[i] = (((value >>> 8) + 1) << (32 - length)) - 1;
        }
        for (int length = 1; length <= 32; length++) {
            int offset = baseOffset + (length - 1) * 8;
            minCode[length] = Bytes.u32(huff, offset) << (32 - length);
            maxCode[length] = ((Bytes.u32(huff, offset + 4) + 1) << (32 - length)) - 1;
        }
    }

    private void loadDictionary(byte[] cdic) {
        if (!Bytes.startsWith(cdic, 0, "CDIC")) {
            throw new ParseException(ErrorCode.BOOK_STRUCTURE_INVALID);
        }
        int phrases = Bytes.index(cdic, 8);
        int bits = Bytes.index(cdic, 12);
        int count = Math.min(1 << bits, phrases - dictionary.size());
        for (int i = 0; i < count; i++) {
            int offset = 16 + Bytes.u16(cdic, 16 + i * 2);
            int header = Bytes.u16(cdic, offset);
            byte[] slice = new byte[header & 0x7FFF];
            System.arraycopy(cdic, offset + 2, slice, 0, slice.length);
            dictionary.add(new Phrase(slice, (header & 0x8000) != 0));
        }
    }

    @Override
    public byte[] decompress(byte[] input) {
        try {
            return unpack(input, 0);
        } catch (IndexOutOfBoundsException ex) {
            throw new ParseException(ErrorCode.BOOK_STRUCTURE_INVALID);
        }
    }

    private byte[] unpack(byte[] input, int depth) {
        if (depth > MAX_DEPTH) {
            throw new ParseException(ErrorCode.BOOK_STRUCTURE_INVALID);
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        long bitsLeft = input.length * 8L;
        int position = 0;
        long window = readWindow(input, position);
        int shift = 32;

        while (true) {
            if (shift <= 0) {
                position += 4;
                window = readWindow(input, position);
                shift += 32;
            }
            long code = (window >>> shift) & 0xFFFFFFFFL;
            int prefix = (int) (code >>> 24);
            int length = cacheLength[prefix];
            long max = cacheMax[prefix];
            if (!cacheTerminal[prefix]) {
                while (code < minCode[length]) {
                    length++;
                }
                max = maxCode[length];
            }
            shift -= length;
            bitsLeft -= length;
            if (bitsLeft < 0) {
                return output.toByteArray();
            }
            int index = (int) ((max - code) >>> (32 - length));
            output.writeBytes(expand(index, depth));
            if (output.size() > MAX_OUTPUT) {
                throw new ParseException(ErrorCode.BOOK_CONTENT_TOO_LARGE);
            }
        }
    }

    private byte[] expand(int index, int depth) {
        Phrase phrase = dictionary.get(index);
        if (!phrase.expanded) {
            phrase.data = unpack(phrase.data, depth + 1);
            phrase.expanded = true;
        }
        return phrase.data;
    }

    // 結尾補零
    private static long readWindow(byte[] input, int position) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            int index = position + i;
            value = value << 8 | (index < input.length ? input[index] & 0xFF : 0);
        }
        return value;
    }
}
