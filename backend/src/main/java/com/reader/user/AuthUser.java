package com.reader.user;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

// 登入者資訊
public record AuthUser(Long id, String username, String passwordHash) implements UserDetails {

    static AuthUser from(User user) {
        return new AuthUser(user.getId(), user.getUsername(), user.getPasswordHash());
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_USER");
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
