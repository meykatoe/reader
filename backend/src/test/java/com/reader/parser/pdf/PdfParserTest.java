package com.reader.parser.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import com.reader.parser.ParsedBook;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PdfParserTest {

    @TempDir
    private Path directory;

    @Test
    void readsPageCountAndCover() throws IOException {
        Path file = directory.resolve("book.pdf");
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.addPage(new PDPage());
            document.getDocumentInformation().setTitle("文件");
            document.save(file.toFile());
        }

        ParsedBook book = new PdfParser().parse(file);

        assertThat(book.title()).isEqualTo("文件");
        assertThat(book.pageCount()).isEqualTo(2);
        assertThat(book.assets()).containsKey("cover.jpg");
    }
}
