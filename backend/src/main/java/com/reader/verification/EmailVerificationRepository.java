package com.reader.verification;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmailAndPurpose(String email, Purpose purpose);

    Optional<EmailVerification> findByTokenHashAndPurpose(String tokenHash, Purpose purpose);

    // 原子扣次數
    @Transactional
    @Modifying
    @Query("""
            update EmailVerification v set v.attempts = v.attempts + 1
            where v.id = :id and v.attempts < :limit
            """)
    int claimAttempt(@Param("id") Long id, @Param("limit") int limit);

    // 僅能刪一次
    @Transactional
    @Modifying
    @Query("delete from EmailVerification v where v.id = :id and v.codeHash = :codeHash")
    int consume(@Param("id") Long id, @Param("codeHash") String codeHash);

    @Transactional
    @Modifying
    @Query("delete from EmailVerification v where v.email = :email and v.purpose = :purpose")
    void deleteTarget(@Param("email") String email, @Param("purpose") Purpose purpose);

    @Transactional
    @Modifying
    @Query("delete from EmailVerification v where v.expiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") Instant cutoff);
}
