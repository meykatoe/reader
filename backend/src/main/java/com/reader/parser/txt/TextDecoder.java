package com.reader.parser.txt;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import org.mozilla.universalchardet.UniversalDetector;

// 編碼偵測
final class TextDecoder {

    private static final Charset FALLBACK = Charset.forName("GB18030");

    private TextDecoder() {
    }

    static String decode(byte[] data) {
        String text = decodeRaw(data);
        return text.startsWith("﻿") ? text.substring(1) : text;
    }

    private static String decodeRaw(byte[] data) {
        if (hasPrefix(data, 0xFF, 0xFE)) {
            return new String(data, StandardCharsets.UTF_16LE);
        }
        if (hasPrefix(data, 0xFE, 0xFF)) {
            return new String(data, StandardCharsets.UTF_16BE);
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(data))
                    .toString();
        } catch (CharacterCodingException ex) {
            return new String(data, detect(data));
        }
    }

    private static Charset detect(byte[] data) {
        UniversalDetector detector = new UniversalDetector();
        detector.handleData(data, 0, data.length);
        detector.dataEnd();
        String name = detector.getDetectedCharset();
        return name != null && Charset.isSupported(name) ? Charset.forName(name) : FALLBACK;
    }

    private static boolean hasPrefix(byte[] data, int first, int second) {
        return data.length >= 2 && (data[0] & 0xFF) == first && (data[1] & 0xFF) == second;
    }
}
