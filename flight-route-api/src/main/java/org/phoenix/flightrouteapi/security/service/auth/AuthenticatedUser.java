package org.phoenix.flightrouteapi.security.service.auth;

import org.phoenix.flightrouteapi.security.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class AuthenticatedUser extends User {

    private final Long userId;
    private final Role role;

    public AuthenticatedUser(Long userId,
                             String username,
                             String passwordHash,
                             Role role,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, passwordHash, authorities);
        this.userId = userId;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }
}
