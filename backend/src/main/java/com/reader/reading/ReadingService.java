package com.reader.reading;

import com.reader.book.Book;
import com.reader.book.BookService;
import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import com.reader.content.Chapter;
import com.reader.content.ChapterRepository;
import com.reader.content.ChapterSummary;
import com.reader.parser.ContentSanitizer;
import com.reader.quota.ReadGuard;
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
    private final ReadGuard readGuard;

    public ReadingService(BookService bookService,
                          ChapterRepository chapterRepository,
                          ReadingProgressRepository progressRepository,
                          ReadGuard readGuard) {
        this.bookService = bookService;
        this.chapterRepository = chapterRepository;
        this.progressRepository = progressRepository;
        this.readGuard = readGuard;
    }

    public List<ChapterSummary> listChapters(Long bookId, Long userId) {
        readGuard.open(bookService.getVisible(bookId, userId), userId);
        return chapterRepository.findSummaries(bookId);
    }

    public ChapterResponse getChapter(Long bookId, int index, Long userId) {
        readGuard.readChapter(bookService.getVisible(bookId, userId), userId);
        Chapter chapter = chapterRepository.findByBookIdAndIndex(bookId, index)
                .orElseThrow(() -> new ApiException(ErrorCode.CHAPTER_NOT_FOUND));
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
            throw new ApiException(ErrorCode.POSITION_OUT_OF_RANGE);
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
