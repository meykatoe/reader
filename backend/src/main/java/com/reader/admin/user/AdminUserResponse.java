package com.reader.admin.user;

import com.reader.user.User;
import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String username,
        String email,
        long bookCount,
        boolean member,
        Instant memberUntil,
        Instant createdAt,
        Instant deletedAt) {

    static AdminUserResponse of(User user, long bookCount) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                bookCount,
                user.isMember(Instant.now()),
                user.getMemberUntil(),
                user.getCreatedAt(),
                user.getDeletedAt());
    }
}
