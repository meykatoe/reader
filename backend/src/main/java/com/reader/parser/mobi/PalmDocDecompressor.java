package com.reader.parser.mobi;

import com.reader.parser.ParseException;
import java.util.Arrays;

// LZ77 解壓
final class PalmDocDecompressor implements Decompressor {

    private static final int MAX_OUTPUT = 64 * 1024;

    @Override
    public byte[] decompress(byte[] input) {
        byte[] output = new byte[MAX_OUTPUT];
        int size = 0;
        int i = 0;
        try {
            while (i < input.length) {
                int c = input[i++] & 0xFF;
                if (c >= 1 && c <= 8) {
                    System.arraycopy(input, i, output, size, c);
                    i += c;
                    size += c;
                } else if (c < 0x80) {
                    output[size++] = (byte) c;
                } else if (c >= 0xC0) {
                    output[size++] = ' ';
                    output[size++] = (byte) (c ^ 0x80);
                } else {
                    int pair = (c << 8 | input[i++] & 0xFF) & 0x3FFF;
                    int distance = pair >> 3;
                    int length = (pair & 7) + 3;
                    if (distance == 0 || distance > size) {
                        throw new ParseException("MOBI 壓縮資料損毀");
                    }
                    // 允許重疊複製
                    for (int n = 0; n < length; n++, size++) {
                        output[size] = output[size - distance];
                    }
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            throw new ParseException("MOBI 壓縮資料損毀");
        }
        return Arrays.copyOf(output, size);
    }
}
