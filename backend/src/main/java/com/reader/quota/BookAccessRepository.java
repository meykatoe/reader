package com.reader.quota;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookAccessRepository extends JpaRepository<BookAccess, Long> {

    boolean existsByUserIdAndKindAndAccessDateAndBookId(
            Long userId, AccessKind kind, LocalDate accessDate, Long bookId);

    int countByUserIdAndKindAndAccessDate(Long userId, AccessKind kind, LocalDate accessDate);

    @Query("""
            select a.bookId from BookAccess a
            where a.userId = :userId and a.kind = :kind and a.accessDate = :date
            """)
    List<Long> findBookIds(@Param("userId") Long userId,
                           @Param("kind") AccessKind kind,
                           @Param("date") LocalDate date);
}
