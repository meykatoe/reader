package com.reader.audit;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserApiLogRepository extends JpaRepository<UserApiLog, Long> {

    List<UserApiLog> findByRequestId(String requestId);

    @Transactional
    @Modifying
    @Query("delete from UserApiLog l where l.createdAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
