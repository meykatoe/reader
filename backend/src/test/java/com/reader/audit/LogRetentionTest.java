package com.reader.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LogRetentionTest {

    @Autowired
    private LogRetentionJob job;

    @Autowired
    private UserApiLogRepository userLogs;

    @Autowired
    private AdminApiLogRepository adminLogs;

    @Test
    void keepsAdminLogsLonger() {
        String old = save(Duration.ofDays(100));
        String recent = save(Duration.ofDays(80));
        String ancient = save(Duration.ofDays(400));

        job.purge();

        assertThat(userLogs.findByRequestId(old)).isEmpty();
        assertThat(userLogs.findByRequestId(recent)).hasSize(1);
        assertThat(adminLogs.findByRequestId(old)).hasSize(1);
        assertThat(adminLogs.findByRequestId(ancient)).isEmpty();
    }

    private String save(Duration age) {
        String id = UUID.randomUUID().toString().substring(0, 16);
        ApiCall call = new ApiCall(id, null, null, "GET", "/api/test", 200, 0, "OK", null,
                "127.0.0.1", 1, Instant.now().minus(age));
        userLogs.save(new UserApiLog(call));
        adminLogs.save(new AdminApiLog(call));
        return id;
    }
}
