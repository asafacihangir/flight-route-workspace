package org.phoenix.flightrouteapi.security.service.auth;

import org.phoenix.flightrouteapi.security.domain.User;
import org.phoenix.flightrouteapi.security.repository.UserRepository;
import org.phoenix.flightrouteapi.security.service.token.JwtService;
import org.phoenix.flightrouteapi.security.util.SecurityUtils;
import org.phoenix.flightrouteapi.security.web.dto.CurrentUserResponse;
import org.phoenix.flightrouteapi.security.web.dto.LoginRequest;
import org.phoenix.flightrouteapi.security.web.dto.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(request.username(), request.password());

        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtService.generateToken(authentication);
        return LoginResponse.bearer(jwt, jwtService.getExpirationSeconds());
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser() {
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new UsernameNotFoundException("No authenticated user"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

        List<String> authorities = AuthorityUtils
                .createAuthorityList("ROLE_" + user.getRole().name())
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                authorities
        );
    }
}
