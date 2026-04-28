package org.phoenix.flightrouteapi.security.web;

import org.phoenix.flightrouteapi.security.service.auth.AuthService;
import org.phoenix.flightrouteapi.security.web.dto.CurrentUserResponse;
import org.phoenix.flightrouteapi.security.web.dto.LoginRequest;
import org.phoenix.flightrouteapi.security.web.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    CurrentUserResponse me() {
        return authService.getCurrentUser();
    }
}
