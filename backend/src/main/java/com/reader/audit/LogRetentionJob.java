package com.reader.audit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 清除過期日誌
@Component
public class LogRetentionJob {

    private static final Logger log = LoggerFactory.getLogger(LogRetentionJob.class);

    private final UserApiLogRepository userLogs;
    private final AdminApiLogRepository adminLogs;
    private final Clock clock;
    private final int userDays;
    private final int adminDays;

    public LogRetentionJob(UserApiLogRepository userLogs,
                           AdminApiLogRepository adminLogs,
                           Clock clock,
                           @Value("${reader.logs.user-retention-days:90}") int userDays,
                           @Value("${reader.logs.admin-retention-days:365}") int adminDays) {
        this.userLogs = userLogs;
        this.adminLogs = adminLogs;
        this.clock = clock;
        this.userDays = userDays;
        this.adminDays = adminDays;
    }

    // 凌晨執行
    @Scheduled(cron = "0 30 3 * * *", zone = "${reader.limits.zone:Asia/Taipei}")
    public void purge() {
        purge("user", userDays, userLogs::deleteOlderThan);
        purge("admin", adminDays, adminLogs::deleteOlderThan);
    }

    private interface Deletion {

        int before(Instant cutoff);
    }

    // 零為永久
    private void purge(String name, int days, Deletion deletion) {
        if (days <= 0) {
            return;
        }
        int removed = deletion.before(clock.instant().minus(Duration.ofDays(days)));
        log.info("Removed {} {} log entries older than {} days", removed, name, days);
    }
}
