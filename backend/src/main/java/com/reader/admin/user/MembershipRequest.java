package com.reader.admin.user;

import jakarta.validation.constraints.Future;
import java.time.Instant;

// 空值為取消
public record MembershipRequest(@Future(message = "MEMBERSHIP_EXPIRY_INVALID") Instant memberUntil) {
}
