package org.phoenix.flightrouteapi.security.domain;

import org.phoenix.flightrouteapi.shared.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    protected User() {
    }

    public User(String username, String passwordHash, Role role) {
        this.username = requireNonBlank(username, "username");
        this.passwordHash = requireNonBlank(passwordHash, "passwordHash");
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }
        this.role = role;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = requireNonBlank(newPasswordHash, "passwordHash");
    }

    private static String requireNonBlank(String v, String field) {
        if (v == null || v.trim().isBlank()) {
            throw new IllegalArgumentException(field + " cannot be null or blank");
        }
        return v.trim();
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

    public Role getRole() {
        return role;
    }
}
