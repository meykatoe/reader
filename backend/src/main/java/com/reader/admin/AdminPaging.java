package com.reader.admin;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class AdminPaging {

    private static final int MAX_SIZE = 100;

    private AdminPaging() {
    }

    // 新資料在前
    public static Pageable of(boolean deleted, int page, int size) {
        String time = deleted ? "deletedAt" : "createdAt";
        return PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, MAX_SIZE),
                Sort.by(Sort.Direction.DESC, time, "id"));
    }
}
