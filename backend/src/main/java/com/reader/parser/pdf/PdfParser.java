package com.reader.parser.pdf;

import com.reader.book.BookFormat;
import com.reader.common.ErrorCode;
import com.reader.parser.BookParser;
import com.reader.parser.ParseException;
import com.reader.parser.ParsedBook;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PdfParser implements BookParser {

    private static final Logger log = LoggerFactory.getLogger(PdfParser.class);

    private static final String COVER_NAME = "cover.jpg";
    private static final float COVER_WIDTH = 400f;
    private static final float MAX_SCALE = 4f;

    @Override
    public BookFormat format() {
        return BookFormat.PDF;
    }

    @Override
    public ParsedBook parse(Path file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.toFile())) {
            if (document.getNumberOfPages() == 0) {
                throw new ParseException(ErrorCode.BOOK_NO_CONTENT);
            }
            PDDocumentInformation information = document.getDocumentInformation();
            byte[] cover = renderCover(document);
            return new ParsedBook(
                    information.getTitle(),
                    information.getAuthor(),
                    List.of(),
                    cover == null ? Map.of() : Map.of(COVER_NAME, cover),
                    cover == null ? null : COVER_NAME,
                    document.getNumberOfPages());
        } catch (InvalidPasswordException ex) {
            throw new ParseException(ErrorCode.PDF_ENCRYPTED);
        }
    }

    // 失敗不擋上傳
    private byte[] renderCover(PDDocument document) {
        try {
            PDRectangle box = document.getPage(0).getCropBox();
            float longest = Math.max(box.getWidth(), box.getHeight());
            float scale = Math.min(COVER_WIDTH / Math.max(longest * 0.75f, 1), MAX_SCALE);
            BufferedImage image = new PDFRenderer(document).renderImage(0, scale, ImageType.RGB);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", output);
            return output.toByteArray();
        } catch (IOException | RuntimeException ex) {
            log.warn("Failed to render PDF cover", ex);
            return null;
        }
    }
}
