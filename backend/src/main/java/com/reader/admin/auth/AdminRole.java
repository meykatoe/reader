package com.reader.admin.auth;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

// 權限對照
public enum AdminRole {
    SUPER(1, EnumSet.allOf(AdminAuthority.class)),
    STANDARD(2, EnumSet.of(AdminAuthority.BOOK_MANAGE));

    private final int code;
    private final Set<AdminAuthority> authorities;

    AdminRole(int code, Set<AdminAuthority> authorities) {
        this.code = code;
        this.authorities = authorities;
    }

    public static Optional<AdminRole> of(int code) {
        return Arrays.stream(values()).filter(role -> role.code == code).findFirst();
    }

    public int code() {
        return code;
    }

    public Set<AdminAuthority> authorities() {
        return Set.copyOf(authorities);
    }
}
