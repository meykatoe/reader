package com.reader.admin.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record AdminPrincipal(Long id, String username, String passwordHash, AdminRole role)
        implements UserDetails {

    // 未知即拒絕
    static Optional<AdminPrincipal> from(Admin admin) {
        return AdminRole.of(admin.getPermission()).map(role -> new AdminPrincipal(
                admin.getId(), admin.getUsername(), admin.getPasswordHash(), role));
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return role.authorities().stream()
                .map(authority -> (GrantedAuthority) new SimpleGrantedAuthority(authority.name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
