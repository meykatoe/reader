package com.reader.quota;

import java.time.Instant;
import java.util.List;

public record QuotaResponse(
        boolean member,
        Instant memberUntil,
        int downloadLimit,
        int downloadUsed,
        List<Long> downloadedBookIds) {
}
