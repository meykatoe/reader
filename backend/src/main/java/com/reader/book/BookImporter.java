package com.reader.book;

import com.reader.content.Chapter;
import com.reader.content.ChapterRepository;
import com.reader.parser.ParsedChapter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class BookImporter {

    private final BookRepository bookRepository;
    private final ChapterRepository chapterRepository;

    BookImporter(BookRepository bookRepository, ChapterRepository chapterRepository) {
        this.bookRepository = bookRepository;
        this.chapterRepository = chapterRepository;
    }

    // 整本寫入
    @Transactional
    public Long save(Book book, List<ParsedChapter> parsedChapters) {
        Long bookId = bookRepository.save(book).getId();
        List<Chapter> chapters = new ArrayList<>();
        for (int i = 0; i < parsedChapters.size(); i++) {
            ParsedChapter parsed = parsedChapters.get(i);
            chapters.add(new Chapter(bookId, i, parsed.title(), parsed.html(), parsed.charCount()));
        }
        chapterRepository.saveAll(chapters);
        return bookId;
    }
}
