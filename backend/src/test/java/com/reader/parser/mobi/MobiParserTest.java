package com.reader.parser.mobi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.reader.parser.ParseException;
import com.reader.parser.ParsedBook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MobiParserTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};

    @TempDir
    private Path directory;

    @Test
    void readsChaptersAndImages() throws IOException {
        byte[] html = ("<html><body><h1>第一章</h1><p>內文一</p><img recindex=\"00001\">"
                + "<mbp:pagebreak/><h1>第二章</h1><p>內文二</p></body></html>")
                .getBytes(StandardCharsets.UTF_8);

        ParsedBook book = parse(mobi(header(1, html.length, 0), html, PNG));

        assertThat(book.title()).isEqualTo("Sample");
        assertThat(book.chapters()).extracting(chapter -> chapter.title())
                .containsExactly("第一章", "第二章");
        assertThat(book.chapters().getFirst().html()).contains("asset:img-1.png");
    }

    @Test
    void decompressesPalmDoc() {
        // 字面值與回溯
        byte[] input = {'a', 'b', 'c', (byte) 0x80, 0x18, (byte) 0xE1, 2, (byte) 0xE4, (byte) 0xB8};

        byte[] output = new PalmDocDecompressor().decompress(input);

        assertThat(output).containsExactly(
                'a', 'b', 'c', 'a', 'b', 'c', ' ', 'a', 0xE4, 0xB8);
    }

    @Test
    void decompressesHuffCdic() {
        // 固定八位元碼
        ByteBuffer huff = ByteBuffer.allocate(24 + 1024 + 256);
        huff.put(0, "HUFF".getBytes(StandardCharsets.US_ASCII));
        huff.putInt(8, 24);
        huff.putInt(12, 24 + 1024);
        for (int i = 0; i < 256; i++) {
            huff.putInt(24 + i * 4, 255 << 8 | 0x80 | 8);
        }

        // 第二項需遞迴
        byte[][] phrases = {"Hi".getBytes(StandardCharsets.US_ASCII), {(byte) 255, (byte) 255}};
        ByteBuffer cdic = ByteBuffer.allocate(64);
        cdic.put(0, "CDIC".getBytes(StandardCharsets.US_ASCII));
        cdic.putInt(8, 2);
        cdic.putInt(12, 8);
        cdic.putShort(16, (short) 4);
        cdic.putShort(18, (short) 8);
        cdic.putShort(20, (short) (0x8000 | 2));
        cdic.put(22, phrases[0]);
        cdic.putShort(24, (short) 2);
        cdic.put(26, phrases[1]);

        byte[] output = new HuffCdicDecompressor(huff.array(), List.of(cdic.array()))
                .decompress(new byte[] {(byte) 255, (byte) 254});

        assertThat(new String(output, StandardCharsets.US_ASCII)).isEqualTo("HiHiHi");
    }

    @Test
    void stripsTrailingEntries() {
        byte[] record = {'h', 'i', 9, 9, (byte) 0x83};

        assertThat(TrailingEntries.strip(record, 0b10)).containsExactly('h', 'i');
    }

    @Test
    void rejectsEncryptedBook() {
        byte[] encrypted = mobi(header(1, 1, 2), new byte[] {'x'});

        assertThatThrownBy(() -> parse(encrypted))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("DRM");
    }

    private ParsedBook parse(byte[] data) throws IOException {
        return new MobiParser().parse(Files.write(directory.resolve("book.mobi"), data));
    }

    private static byte[] header(int compression, int textLength, int encryption) {
        byte[] name = "Sample".getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(248 + name.length);
        buffer.putShort(0, (short) compression);
        buffer.putInt(4, textLength);
        buffer.putShort(8, (short) 1);
        buffer.putShort(12, (short) encryption);
        buffer.put(16, "MOBI".getBytes(StandardCharsets.US_ASCII));
        buffer.putInt(20, 232);
        buffer.putInt(28, 65001);
        buffer.putInt(84, 248);
        buffer.putInt(88, name.length);
        // 首張圖片位置
        buffer.putInt(108, 2);
        buffer.put(248, name);
        return buffer.array();
    }

    private static byte[] mobi(byte[]... records) {
        List<byte[]> list = List.of(records);
        int offset = 78 + list.size() * 8;
        ByteBuffer head = ByteBuffer.allocate(offset);
        head.put(60, "BOOKMOBI".getBytes(StandardCharsets.US_ASCII));
        head.putShort(76, (short) list.size());
        for (int i = 0; i < list.size(); i++) {
            head.putInt(78 + i * 8, offset);
            offset += list.get(i).length;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.writeBytes(head.array());
        list.forEach(output::writeBytes);
        return output.toByteArray();
    }
}
