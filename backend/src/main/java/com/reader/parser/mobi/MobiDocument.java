package com.reader.parser.mobi;

import com.reader.parser.ParseException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class MobiDocument {

    private static final int UNCOMPRESSED = 1;
    private static final int PALMDOC = 2;
    private static final int HUFF_CDIC = 17480;
    private static final long MAX_TEXT_BYTES = 50L * 1024 * 1024;

    private final PalmDatabase database;
    private final MobiHeader header;

    MobiDocument(byte[] data) {
        this.database = new PalmDatabase(data);
        this.header = MobiHeader.parse(database.record(0));
    }

    MobiHeader header() {
        return header;
    }

    String html() {
        if (header.textLength() > MAX_TEXT_BYTES) {
            throw new ParseException("書籍文字內容過大");
        }
        Decompressor decompressor = createDecompressor();
        ByteArrayOutputStream text = new ByteArrayOutputStream();
        for (int i = 1; i <= header.textRecords(); i++) {
            byte[] record = TrailingEntries.strip(database.record(i), header.trailingFlags());
            text.writeBytes(decompressor.decompress(record));
            if (text.size() > MAX_TEXT_BYTES) {
                throw new ParseException("書籍文字內容過大");
            }
        }
        byte[] bytes = text.toByteArray();
        int length = (int) Math.min(bytes.length, header.textLength());
        return new String(bytes, 0, length, header.charset());
    }

    // 序號從一起算
    Optional<byte[]> image(int number) {
        long index = (long) header.firstImageRecord() + number - 1;
        if (number < 1 || header.firstImageRecord() < 1 || index >= database.size()) {
            return Optional.empty();
        }
        return Optional.of(database.record((int) index));
    }

    private Decompressor createDecompressor() {
        return switch (header.compression()) {
            case UNCOMPRESSED -> input -> input;
            case PALMDOC -> new PalmDocDecompressor();
            case HUFF_CDIC -> createHuffDecompressor();
            default -> throw new ParseException("不支援的 MOBI 壓縮格式");
        };
    }

    private Decompressor createHuffDecompressor() {
        List<byte[]> dictionaries = new ArrayList<>();
        for (int i = 1; i < header.huffCount(); i++) {
            dictionaries.add(database.record(header.huffRecord() + i));
        }
        return new HuffCdicDecompressor(database.record(header.huffRecord()), dictionaries);
    }
}
