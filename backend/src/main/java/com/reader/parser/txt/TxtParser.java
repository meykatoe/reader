package com.reader.parser.txt;

import com.reader.book.BookFormat;
import com.reader.parser.BookParser;
import com.reader.parser.ParseException;
import com.reader.parser.ParsedBook;
import com.reader.parser.ParsedChapter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.jsoup.nodes.Entities;
import org.springframework.stereotype.Component;

@Component
public class TxtParser implements BookParser {

    private static final Pattern HEADING = Pattern.compile(
            "^(第\\s*[0-9０-９零〇一二三四五六七八九十百千萬万兩两]+\\s*[章節节回卷部集篇](?:[\\s：:、.．].{0,40})?"
                    + "|(?i:chapter)\\s+[0-9IVXLC]+\\b.{0,60}"
                    + "|(?:序章|序言|楔子|引子|前言|尾聲|尾声|後記|后记|番外)(?:[\\s：:].{0,40})?)$");

    // 單章上限
    private static final int MAX_CHAPTER_CHARS = 20_000;

    @Override
    public BookFormat format() {
        return BookFormat.TXT;
    }

    @Override
    public ParsedBook parse(Path file) throws IOException {
        List<ParsedChapter> chapters = split(TextDecoder.decode(Files.readAllBytes(file)));
        if (chapters.isEmpty()) {
            throw new ParseException("檔案內沒有可閱讀的文字");
        }
        return ParsedBook.textOnly(chapters);
    }

    private List<ParsedChapter> split(String text) {
        List<ParsedChapter> chapters = new ArrayList<>();
        boolean hasHeading = text.lines().map(String::strip).anyMatch(this::isHeading);
        Section section = new Section(hasHeading ? "開始" : null);

        for (String line : (Iterable<String>) text.lines()::iterator) {
            String paragraph = line.strip();
            if (paragraph.isEmpty()) {
                continue;
            }
            if (isHeading(paragraph)) {
                section.flushTo(chapters);
                section = new Section(paragraph);
                continue;
            }
            if (section.length >= MAX_CHAPTER_CHARS) {
                section.flushTo(chapters);
                section = section.next();
            }
            section.add(paragraph);
        }
        section.flushTo(chapters);
        return chapters;
    }

    private boolean isHeading(String line) {
        return HEADING.matcher(line).matches();
    }

    private static final class Section {

        private final String title;
        private final int part;
        private final StringBuilder html = new StringBuilder();
        private int length;

        Section(String title) {
            this(title, 1);
        }

        private Section(String title, int part) {
            this.title = title;
            this.part = part;
        }

        Section next() {
            return new Section(title, part + 1);
        }

        void add(String paragraph) {
            html.append("<p>").append(Entities.escape(paragraph)).append("</p>");
            length += paragraph.length();
        }

        void flushTo(List<ParsedChapter> chapters) {
            // 略過空白前言
            if (length == 0 && (title == null || title.equals("開始"))) {
                return;
            }
            String name = chapterName(chapters.size());
            String heading = "<h2>" + Entities.escape(name) + "</h2>";
            chapters.add(new ParsedChapter(name, heading + html, length));
        }

        private String chapterName(int count) {
            if (title == null) {
                return "第 " + (count + 1) + " 部分";
            }
            String name = part == 1 ? title : title + "（" + part + "）";
            return name.length() > 255 ? name.substring(0, 255) : name;
        }
    }
}
