package com.reader.admin;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class AdminPaging {

    private static final int MAX_SIZE = 100;

    private AdminPaging() {
    }

    // 新資料在前
    public static Pageable newestFirst(int page, int size) {
        return PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, MAX_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    }
}
