package com.reader.user;

import com.reader.common.HardDeleteGuard;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@EntityListeners(HardDeleteGuard.class)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String username;

    // 舊帳號為空值
    @Column(unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // 會員到期日
    @Column(name = "member_until")
    private Instant memberUntil;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected User() {
    }

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = Instant.now();
    }

    public User(String username, String passwordHash, String email) {
        this(username, passwordHash);
        this.email = email;
    }

    public void softDelete(Instant when) {
        this.deletedAt = when;
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public boolean isMember(Instant now) {
        return memberUntil != null && memberUntil.isAfter(now);
    }

    // 空值即取消
    public void changeMembership(Instant memberUntil) {
        this.memberUntil = memberUntil;
    }

    public Instant getMemberUntil() {
        return memberUntil;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
