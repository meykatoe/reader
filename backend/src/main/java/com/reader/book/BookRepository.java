package com.reader.book;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("""
            select b from Book b left join fetch b.owner o
            where b.privateBook = false or o.id = :userId
            order by b.createdAt desc
            """)
    List<Book> findVisible(@Param("userId") Long userId);

    @Query("select b from Book b join fetch b.owner o where o.id = :userId order by b.createdAt desc")
    List<Book> findOwned(@Param("userId") Long userId);

    @Query("select b from Book b left join fetch b.owner where b.id = :id")
    Optional<Book> findWithOwner(@Param("id") Long id);

    // 用戶書籍數
    @Query("""
            select b.owner.id as ownerId, count(b) as total from Book b
            where b.owner.id in :ownerIds group by b.owner.id
            """)
    List<OwnerBookCount> countByOwners(@Param("ownerIds") Collection<Long> ownerIds);

    // 後臺搜尋
    @EntityGraph(attributePaths = "owner")
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String author, Pageable pageable);
}
