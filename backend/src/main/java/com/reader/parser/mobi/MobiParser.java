package com.reader.parser.mobi;

import com.reader.book.BookFormat;
import com.reader.parser.AssetCollector;
import com.reader.parser.BookParser;
import com.reader.parser.ContentSanitizer;
import com.reader.parser.ParseException;
import com.reader.parser.ParsedBook;
import com.reader.parser.ParsedChapter;
import com.reader.parser.Titles;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class MobiParser implements BookParser {

    // 分章標記
    private static final Pattern BREAK = Pattern.compile(
            "<\\s*(?:mbp:)?pagebreak[^>]*>|(?=<\\?xml\\s)", Pattern.CASE_INSENSITIVE);

    private static final Pattern EMBED = Pattern.compile("kindle:embed:([0-9A-V]+)");

    @Override
    public BookFormat format() {
        return BookFormat.MOBI;
    }

    @Override
    public ParsedBook parse(Path file) throws IOException {
        MobiDocument document = new MobiDocument(Files.readAllBytes(file));
        MobiHeader header = document.header();
        AssetCollector assets = new AssetCollector();

        String coverName = header.coverOffset() == null
                ? null
                : addImage(document, assets, header.coverOffset() + 1).orElse(null);

        List<ParsedChapter> chapters = readChapters(document, assets);
        if (chapters.isEmpty()) {
            throw new ParseException("MOBI 內沒有可閱讀的內容");
        }
        return new ParsedBook(header.title(), header.author(), chapters, assets.assets(), coverName);
    }

    private List<ParsedChapter> readChapters(MobiDocument document, AssetCollector assets) {
        List<ParsedChapter> chapters = new ArrayList<>();
        for (String segment : BREAK.split(document.html())) {
            Element body = Jsoup.parse(segment).body();
            String title = Titles.of(body, "第 " + (chapters.size() + 1) + " 節");
            ContentSanitizer.toChapter(title, body, image -> imageNumber(image)
                            .flatMap(number -> addImage(document, assets, number)))
                    .ifPresent(chapters::add);
        }
        return chapters;
    }

    private static Optional<String> addImage(
            MobiDocument document, AssetCollector assets, int number) {
        return assets.add("record-" + number, () -> document.image(number).orElse(null));
    }

    // 新舊兩種寫法
    private static Optional<Integer> imageNumber(Element image) {
        String recordIndex = image.attr("recindex");
        if (recordIndex.matches("\\d{1,6}")) {
            return Optional.of(Integer.parseInt(recordIndex));
        }
        Matcher matcher = EMBED.matcher(image.attr("src"));
        if (matcher.find() && matcher.group(1).length() <= 6) {
            return Optional.of(Integer.parseInt(matcher.group(1), 32));
        }
        return Optional.empty();
    }
}
