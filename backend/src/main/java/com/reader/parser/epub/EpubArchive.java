package com.reader.parser.epub;

import com.reader.parser.ParseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

final class EpubArchive implements AutoCloseable {

    // 防壓縮炸彈
    private static final int MAX_ENTRY_BYTES = 20 * 1024 * 1024;
    private static final long MAX_TOTAL_BYTES = 300L * 1024 * 1024;

    private final ZipFile zip;
    private long totalRead;

    EpubArchive(Path file) throws IOException {
        this.zip = new ZipFile(file.toFile());
    }

    boolean exists(String path) {
        return zip.getEntry(path) != null;
    }

    // 缺檔回傳空
    byte[] read(String path) {
        ZipEntry entry = zip.getEntry(path);
        if (entry == null) {
            return null;
        }
        try (InputStream input = zip.getInputStream(entry)) {
            byte[] data = input.readNBytes(MAX_ENTRY_BYTES + 1);
            totalRead += data.length;
            if (data.length > MAX_ENTRY_BYTES || totalRead > MAX_TOTAL_BYTES) {
                throw new ParseException("書籍解壓後內容過大");
            }
            return data;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    Document readXml(String path) {
        byte[] data = read(path);
        if (data == null) {
            throw new ParseException("EPUB 缺少必要檔案");
        }
        try {
            Document document = Jsoup.parse(
                    new ByteArrayInputStream(data), null, "", Parser.xmlParser());
            document.outputSettings().syntax(Document.OutputSettings.Syntax.html).prettyPrint(false);
            return document;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }
}
