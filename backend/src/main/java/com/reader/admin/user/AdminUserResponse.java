package com.reader.admin.user;

import com.reader.user.User;
import java.time.Instant;

public record AdminUserResponse(Long id, String username, long bookCount, Instant createdAt) {

    static AdminUserResponse of(User user, long bookCount) {
        return new AdminUserResponse(
                user.getId(), user.getUsername(), bookCount, user.getCreatedAt());
    }
}
