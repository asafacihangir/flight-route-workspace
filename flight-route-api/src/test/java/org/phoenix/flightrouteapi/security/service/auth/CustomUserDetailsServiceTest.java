package org.phoenix.flightrouteapi.security.service.auth;

import org.phoenix.flightrouteapi.security.domain.Role;
import org.phoenix.flightrouteapi.security.domain.User;
import org.phoenix.flightrouteapi.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    private static final String USERNAME = "alice";
    private static final String PASSWORD_HASH = "{bcrypt}$2a$10$abcdefghijklmnopqrstuv";

    private UserRepository userRepository;
    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void returnedPasswordEqualsEntityPasswordHash() {
        User user = new User(USERNAME, PASSWORD_HASH, Role.ADMIN);
        when(userRepository.findByUsername(eq(USERNAME))).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername(USERNAME);

        assertThat(details.getPassword()).isEqualTo(PASSWORD_HASH);
    }

    @Test
    void returnedPrincipalIsAuthenticatedUserCarryingRole() {
        User user = new User(USERNAME, PASSWORD_HASH, Role.AGENCY);
        when(userRepository.findByUsername(eq(USERNAME))).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername(USERNAME);

        assertThat(details).isInstanceOf(AuthenticatedUser.class);
        AuthenticatedUser authenticated = (AuthenticatedUser) details;
        assertThat(authenticated.getRole()).isEqualTo(Role.AGENCY);
        assertThat(authenticated.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_AGENCY");
    }

    @Test
    void lowercasesUsernameLookup() {
        User user = new User(USERNAME, PASSWORD_HASH, Role.ADMIN);
        when(userRepository.findByUsername(eq(USERNAME))).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("ALICE");

        assertThat(details.getUsername()).isEqualTo(USERNAME);
    }

    @Test
    void throwsWhenUserNotFound() {
        when(userRepository.findByUsername(eq("missing"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
