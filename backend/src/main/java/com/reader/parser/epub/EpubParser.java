package com.reader.parser.epub;

import com.reader.book.BookFormat;
import com.reader.parser.AssetCollector;
import com.reader.parser.BookParser;
import com.reader.parser.ContentSanitizer;
import com.reader.parser.ParseException;
import com.reader.parser.ParsedBook;
import com.reader.parser.ParsedChapter;
import com.reader.parser.Titles;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class EpubParser implements BookParser {

    private static final String ENCRYPTION = "META-INF/encryption.xml";

    @Override
    public BookFormat format() {
        return BookFormat.EPUB;
    }

    @Override
    public ParsedBook parse(Path file) throws IOException {
        try (EpubArchive archive = new EpubArchive(file)) {
            EpubPackage pack = EpubPackage.read(archive);
            rejectEncrypted(archive, pack.spine());

            AssetCollector assets = new AssetCollector();
            String coverName = Optional.ofNullable(pack.coverPath())
                    .flatMap(path -> assets.add(path, () -> archive.read(path)))
                    .orElse(null);

            List<ParsedChapter> chapters = readChapters(archive, pack, assets);
            if (chapters.isEmpty()) {
                throw new ParseException("EPUB 內沒有可閱讀的內容");
            }
            return new ParsedBook(pack.title(), pack.author(), chapters, assets.assets(), coverName);
        }
    }

    private List<ParsedChapter> readChapters(
            EpubArchive archive, EpubPackage pack, AssetCollector assets) {
        List<ParsedChapter> chapters = new ArrayList<>();
        for (String path : pack.spine()) {
            if (!archive.exists(path)) {
                continue;
            }
            Document document = archive.readXml(path);
            Element body = document.selectFirst("body");
            if (body == null) {
                continue;
            }
            String title = pack.tocTitles().containsKey(path)
                    ? pack.tocTitles().get(path)
                    : Titles.of(body, "第 " + (chapters.size() + 1) + " 節");
            ContentSanitizer.toChapter(title, body, image -> {
                String source = image.hasAttr("src") ? image.attr("src")
                        : image.hasAttr("xlink:href") ? image.attr("xlink:href")
                        : image.attr("href");
                if (source.isBlank() || source.contains(":")) {
                    return Optional.empty();
                }
                String target = EpubPaths.resolve(path, source);
                return assets.add(target, () -> archive.read(target));
            }).ifPresent(chapters::add);
        }
        return chapters;
    }

    // 拒絕版權保護
    private void rejectEncrypted(EpubArchive archive, List<String> spine) {
        if (!archive.exists(ENCRYPTION)) {
            return;
        }
        boolean locked = archive.readXml(ENCRYPTION).getAllElements().stream()
                .filter(element -> element.tagName().endsWith("CipherReference"))
                .anyMatch(element -> spine.contains(EpubPaths.resolve("", element.attr("URI"))));
        if (locked) {
            throw new ParseException("不支援受版權保護（DRM）的書籍");
        }
    }
}
