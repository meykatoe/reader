package com.reader.parser.txt;

import static org.assertj.core.api.Assertions.assertThat;

import com.reader.parser.ParsedBook;
import com.reader.parser.ParsedChapter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TxtParserTest {

    private final TxtParser parser = new TxtParser();

    @TempDir
    private Path directory;

    @Test
    void splitsByHeading() throws IOException {
        ParsedBook book = parse("第一章 起點\n　　第一段。\n\n第二段 <b>。\n第二章\n結束。", "UTF-8");

        assertThat(book.chapters()).extracting(ParsedChapter::title)
                .containsExactly("第一章 起點", "第二章");
        assertThat(book.chapters().getFirst().html())
                .contains("<p>第一段。</p>", "&lt;b&gt;");
    }

    @ParameterizedTest
    @ValueSource(strings = {"GB18030", "Big5"})
    void detectsLegacyEncoding(String charset) throws IOException {
        String line = charset.equals("Big5")
                ? "這是一個繁體中文的測試段落，我們需要正確辨識編碼，讓讀者可以順利閱讀。"
                : "这是一个简体中文的测试段落，我们需要正确识别编码，让读者可以顺利阅读。";

        ParsedBook book = parse((line + "\n").repeat(20), charset);

        assertThat(book.chapters().getFirst().html()).contains(line);
    }

    @Test
    void splitsLongTextWithoutHeading() throws IOException {
        ParsedBook book = parse(("很長的段落".repeat(100) + "\n").repeat(90), "UTF-8");

        assertThat(book.chapters()).hasSizeGreaterThan(1);
        assertThat(book.chapters().getFirst().title()).isEqualTo("第 1 部分");
    }

    private ParsedBook parse(String text, String charset) throws IOException {
        Path file = Files.write(directory.resolve("book.txt"), text.getBytes(Charset.forName(charset)));
        return parser.parse(file);
    }
}
