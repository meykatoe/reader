package com.reader.parser.epub;

import static org.assertj.core.api.Assertions.assertThat;

import com.reader.parser.ParsedBook;
import com.reader.parser.ParsedChapter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EpubParserTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};

    @TempDir
    private Path directory;

    @Test
    void readsMetadataChaptersAndImages() throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("META-INF/container.xml", text("""
                <container><rootfiles><rootfile full-path="OEBPS/book.opf"/></rootfiles></container>
                """));
        entries.put("OEBPS/book.opf", text("""
                <package xmlns:dc="http://purl.org/dc/elements/1.1/">
                  <metadata><dc:title>測試書</dc:title><dc:creator>作者</dc:creator></metadata>
                  <manifest>
                    <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
                    <item id="c1" href="text/ch%201.xhtml" media-type="application/xhtml+xml"/>
                    <item id="img" href="images/a.png" media-type="image/png" properties="cover-image"/>
                  </manifest>
                  <spine><itemref idref="c1"/></spine>
                </package>
                """));
        entries.put("OEBPS/toc.ncx", text("""
                <ncx><navMap><navPoint><navLabel><text>開端</text></navLabel>
                <content src="text/ch%201.xhtml#top"/></navPoint></navMap></ncx>
                """));
        entries.put("OEBPS/text/ch 1.xhtml", text("""
                <html><head><title/></head><body>
                <script>alert(1)</script><a id="top"/>
                <p onclick="alert(2)" style="color:red">內文</p>
                <img src="../images/a.png"/><img src="https://example.com/track.png"/>
                </body></html>
                """));
        entries.put("OEBPS/images/a.png", PNG);

        ParsedBook book = new EpubParser().parse(zip(entries));

        assertThat(book.title()).isEqualTo("測試書");
        assertThat(book.author()).isEqualTo("作者");
        assertThat(book.coverName()).isEqualTo("img-1.png");
        assertThat(book.chapters()).extracting(ParsedChapter::title).containsExactly("開端");
        assertThat(book.chapters().getFirst().html())
                .contains("<p>內文</p>", "src=\"asset:img-1.png\"")
                .doesNotContain("script", "onclick", "style", "example.com");
    }

    private static byte[] text(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private Path zip(Map<String, byte[]> entries) throws IOException {
        Path file = directory.resolve("book.epub");
        try (OutputStream output = Files.newOutputStream(file);
             ZipOutputStream zip = new ZipOutputStream(output)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue());
            }
        }
        return file;
    }
}
