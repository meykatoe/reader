package com.reader.reading;

import com.reader.common.Result;
import com.reader.content.ChapterSummary;
import com.reader.reading.dto.ChapterResponse;
import com.reader.reading.dto.ProgressDto;
import com.reader.user.AuthUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books/{id}")
public class ReadingController {

    private final ReadingService readingService;

    public ReadingController(ReadingService readingService) {
        this.readingService = readingService;
    }

    @GetMapping("/chapters")
    public Result<List<ChapterSummary>> chapters(@AuthenticationPrincipal AuthUser user,
                                                 @PathVariable Long id) {
        return Result.ok(readingService.listChapters(id, user.id()));
    }

    @GetMapping("/chapters/{index}")
    public Result<ChapterResponse> chapter(@AuthenticationPrincipal AuthUser user,
                                           @PathVariable Long id,
                                           @PathVariable int index) {
        return Result.ok(readingService.getChapter(id, index, user.id()));
    }

    @GetMapping("/progress")
    public Result<ProgressDto> progress(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return Result.ok(readingService.getProgress(id, user.id()));
    }

    @PutMapping("/progress")
    public Result<Void> saveProgress(@AuthenticationPrincipal AuthUser user,
                                     @PathVariable Long id,
                                     @Valid @RequestBody ProgressDto progress) {
        readingService.saveProgress(id, user.id(), progress);
        return Result.ok();
    }
}
