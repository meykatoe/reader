package com.reader.admin.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// 資料庫新增
@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private int permission;

    protected Admin() {
    }

    public Admin(String username, String passwordHash, int permission) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.permission = permission;
    }

    public void changePermission(int permission) {
        this.permission = permission;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public int getPermission() {
        return permission;
    }
}
