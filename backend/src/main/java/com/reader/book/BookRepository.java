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
            where b.deletedAt is null and (b.privateBook = false or o.id = :userId)
            order by b.createdAt desc
            """)
    List<Book> findVisible(@Param("userId") Long userId);

    @Query("""
            select b from Book b join fetch b.owner o
            where b.deletedAt is null and o.id = :userId
            order by b.createdAt desc
            """)
    List<Book> findOwned(@Param("userId") Long userId);

    @Query("""
            select b from Book b left join fetch b.owner
            where b.id = :id and b.deletedAt is null
            """)
    Optional<Book> findActive(@Param("id") Long id);

    @Query("""
            select b from Book b left join fetch b.owner
            where b.id = :id and b.deletedAt is not null
            """)
    Optional<Book> findDeleted(@Param("id") Long id);

    List<Book> findByOwnerIdAndDeletionCause(Long ownerId, DeletionCause cause);

    // 用戶書籍數
    @Query("""
            select b.owner.id as ownerId, count(b) as total from Book b
            where b.deletedAt is null and b.owner.id in :ownerIds
            group by b.owner.id
            """)
    List<OwnerBookCount> countByOwners(@Param("ownerIds") Collection<Long> ownerIds);

    // 後臺搜尋
    @EntityGraph(attributePaths = "owner")
    @Query(value = """
            select b from Book b
            where case when b.deletedAt is null then false else true end = :deleted
              and (locate(lower(:keyword), lower(b.title)) > 0
                   or locate(lower(:keyword), lower(b.author)) > 0)
            """,
            countQuery = """
            select count(b) from Book b
            where case when b.deletedAt is null then false else true end = :deleted
              and (locate(lower(:keyword), lower(b.title)) > 0
                   or locate(lower(:keyword), lower(b.author)) > 0)
            """)
    Page<Book> search(@Param("keyword") String keyword,
                      @Param("deleted") boolean deleted,
                      Pageable pageable);
}
