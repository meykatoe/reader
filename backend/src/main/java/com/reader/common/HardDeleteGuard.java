package com.reader.common;

import jakarta.persistence.PreRemove;

// 禁止硬刪除
public class HardDeleteGuard {

    @PreRemove
    void reject(Object entity) {
        throw new IllegalStateException(
                "Hard delete is not allowed for " + entity.getClass().getSimpleName());
    }
}
