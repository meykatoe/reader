package com.reader.admin.auth;

import java.util.Set;

public record AdminResponse(Long id, String username, int permission, Set<AdminAuthority> authorities) {

    static AdminResponse of(AdminPrincipal admin) {
        return new AdminResponse(
                admin.id(), admin.username(), admin.role().code(), admin.role().authorities());
    }
}
