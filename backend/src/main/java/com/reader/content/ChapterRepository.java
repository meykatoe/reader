package com.reader.content;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    // 不載入內文
    @Query("""
            select new com.reader.content.ChapterSummary(c.index, c.title, c.charCount)
            from Chapter c where c.bookId = :bookId order by c.index
            """)
    List<ChapterSummary> findSummaries(@Param("bookId") Long bookId);

    Optional<Chapter> findByBookIdAndIndex(Long bookId, int index);

    int countByBookId(Long bookId);
}
