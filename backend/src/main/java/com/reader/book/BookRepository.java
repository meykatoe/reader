package com.reader.book;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("""
            select b from Book b join fetch b.owner o
            where b.privateBook = false or o.id = :userId
            order by b.createdAt desc
            """)
    List<Book> findVisible(@Param("userId") Long userId);

    @Query("select b from Book b join fetch b.owner o where o.id = :userId order by b.createdAt desc")
    List<Book> findOwned(@Param("userId") Long userId);

    @Query("select b from Book b join fetch b.owner where b.id = :id")
    Optional<Book> findWithOwner(@Param("id") Long id);
}
