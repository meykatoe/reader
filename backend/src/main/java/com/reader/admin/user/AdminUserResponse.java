package com.reader.admin.user;

import com.reader.user.User;
import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String username,
        String email,
        long bookCount,
        long privateBookCount,
        boolean member,
        Instant memberUntil,
        Instant createdAt) {

    static AdminUserResponse of(User user, long bookCount, long privateBookCount) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                bookCount,
                privateBookCount,
                user.isMember(Instant.now()),
                user.getMemberUntil(),
                user.getCreatedAt());
    }
}
