package com.reader.reading;

import com.reader.book.Book;
import com.reader.book.BookService;
import com.reader.common.ApiException;
import com.reader.content.Chapter;
import com.reader.content.ChapterRepository;
import com.reader.content.ChapterSummary;
import com.reader.parser.ContentSanitizer;
import com.reader.reading.dto.ChapterResponse;
import com.reader.reading.dto.ProgressDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReadingService {

    private static final String ASSET_SOURCE = "src=\"" + ContentSanitizer.ASSET_PREFIX;

    private final BookService bookService;
    private final ChapterRepository chapterRepository;
    private final ReadingProgressRepository progressRepository;

    public ReadingService(BookService bookService,
                          ChapterRepository chapterRepository,
                          ReadingProgressRepository progressRepository) {
        this.bookService = bookService;
        this.chapterRepository = chapterRepository;
        this.progressRepository = progressRepository;
    }

    public List<ChapterSummary> listChapters(Long bookId, Long userId) {
        bookService.getVisible(bookId, userId);
        return chapterRepository.findSummaries(bookId);
    }

    public ChapterResponse getChapter(Long bookId, int index, Long userId) {
        bookService.getVisible(bookId, userId);
        Chapter chapter = chapterRepository.findByBookIdAndIndex(bookId, index)
                .orElseThrow(() -> ApiException.notFound("找不到章節"));
        // 換成實際網址
        String html = chapter.getContent()
                .replace(ASSET_SOURCE, "src=\"/api/books/" + bookId + "/assets/");
        return new ChapterResponse(chapter.getIndex(), chapter.getTitle(), html);
    }

    public ProgressDto getProgress(Long bookId, Long userId) {
        bookService.getVisible(bookId, userId);
        return progressRepository.findByUserIdAndBookId(userId, bookId)
                .map(progress -> new ProgressDto(progress.getLocation(), progress.getRatio()))
                .orElse(ProgressDto.START);
    }

    public void saveProgress(Long bookId, Long userId, ProgressDto update) {
        Book book = bookService.getVisible(bookId, userId);
        if (update.location() >= locationCount(book)) {
            throw ApiException.badRequest("位置超出範圍");
        }
        ReadingProgress progress = progressRepository.findByUserIdAndBookId(userId, bookId)
                .orElseGet(() -> new ReadingProgress(userId, bookId));
        progress.moveTo(update.location(), update.ratio());
        progressRepository.save(progress);
    }

    private int locationCount(Book book) {
        return book.getPageCount() != null
                ? book.getPageCount()
                : chapterRepository.countByBookId(book.getId());
    }
}
