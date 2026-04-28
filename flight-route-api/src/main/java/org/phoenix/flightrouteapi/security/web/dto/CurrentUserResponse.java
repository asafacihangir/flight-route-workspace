package org.phoenix.flightrouteapi.security.web.dto;

import org.phoenix.flightrouteapi.security.domain.Role;

import java.util.List;

public record CurrentUserResponse(
        Long id,
        String username,
        Role role,
        List<String> authorities
) {
}
