package com.reader.user;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    boolean existsByUsername(String username);

    @Query("select u.passwordHash from User u where u.id = :id and u.deletedAt is null")
    Optional<String> findPasswordHash(@Param("id") Long id);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> lockById(@Param("id") Long id);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Page<User> findByUsernameContainingIgnoreCaseAndDeletedAtIsNull(
            String username, Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCaseAndDeletedAtIsNotNull(
            String username, Pageable pageable);
}
