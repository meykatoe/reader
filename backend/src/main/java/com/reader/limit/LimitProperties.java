package com.reader.limit;

import java.time.Duration;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("reader.limits")
public record LimitProperties(
        @DefaultValue("5") int loginAttempts,
        @DefaultValue("15m") Duration loginWindow,
        @DefaultValue("3") int registrations,
        @DefaultValue("1h") Duration registrationWindow,
        @DefaultValue("10") int memberDownloads,
        @DefaultValue("1") int freeDownloads,
        @DefaultValue("30") int dailyReadBooks,
        @DefaultValue("60") int chaptersPerMinute,
        // 換日時區
        @DefaultValue("Asia/Taipei") ZoneId zone) {
}
