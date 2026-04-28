package org.phoenix.flightrouteapi.security.service.token;

import com.nimbusds.jose.JWSAlgorithm;
import org.phoenix.flightrouteapi.security.config.properties.JwtProperties;
import org.phoenix.flightrouteapi.security.domain.Role;
import org.phoenix.flightrouteapi.security.service.auth.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret-32";
    private static final int EXPIRATION_MINUTES = 60;
    private static final long EXPIRATION_SECONDS = EXPIRATION_MINUTES * 60L;

    private JwtService jwtService;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(props(SECRET, EXPIRATION_MINUTES));

        SecretKeySpec key = new SecretKeySpec(
                SECRET.getBytes(StandardCharsets.UTF_8), JWSAlgorithm.HS256.getName());
        jwtDecoder = NimbusJwtDecoder.withSecretKey(key).build();
    }

    private static JwtProperties props(String secret, int minutes) {
        JwtProperties p = new JwtProperties();
        p.setSecret(secret);
        p.setExpirationMinutes(minutes);
        return p;
    }

    private static Authentication authFor(Long userId, String username, Role role, String... authorities) {
        AuthenticatedUser principal = new AuthenticatedUser(
                userId, username, "", role,
                Arrays.stream(authorities).map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    @Test
    void generatesTokenWithExpectedClaims() {
        String token = jwtService.generateToken(authFor(42L, "alice", Role.ADMIN, "ROLE_ADMIN"));

        Jwt decoded = jwtDecoder.decode(token);

        assertThat(decoded.getSubject()).isEqualTo("alice");
        assertThat((Object) decoded.getClaim("userId")).isEqualTo(42L);
        assertThat(decoded.getClaimAsString("auth")).isEqualTo("ROLE_ADMIN");
        assertThat(decoded.getIssuedAt()).isNotNull();
        assertThat(decoded.getExpiresAt()).isNotNull();
    }

    @Test
    void expirationMatchesConfiguredMinutes() {
        String token = jwtService.generateToken(authFor(7L, "bob", Role.AGENCY, "ROLE_AGENCY"));
        Jwt decoded = jwtDecoder.decode(token);

        long actualSeconds = decoded.getExpiresAt().getEpochSecond()
                - decoded.getIssuedAt().getEpochSecond();
        assertThat(actualSeconds).isEqualTo(EXPIRATION_SECONDS);
    }

    @Test
    void issuedAtIsAroundNow() {
        Instant before = Instant.now();
        String token = jwtService.generateToken(authFor(7L, "bob", Role.AGENCY, "ROLE_AGENCY"));
        Jwt decoded = jwtDecoder.decode(token);

        assertThat(decoded.getIssuedAt().getEpochSecond())
                .isBetween(before.getEpochSecond() - 1, Instant.now().getEpochSecond() + 1);
    }

    @Test
    void rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtService(props("too-short", EXPIRATION_MINUTES)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void exposesExpirationSeconds() {
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(EXPIRATION_SECONDS);
    }

    @Test
    void generatesTokenFromAuthenticationWithAuthClaim() {
        Authentication auth = authFor(42L, "alice", Role.ADMIN, "ROLE_ADMIN", "ROLE_AGENCY");

        String token = jwtService.generateToken(auth);
        Jwt decoded = jwtDecoder.decode(token);

        assertThat(decoded.getSubject()).isEqualTo("alice");
        assertThat(decoded.getClaimAsString("auth")).isEqualTo("ROLE_ADMIN ROLE_AGENCY");
        assertThat((Object) decoded.getClaim("userId")).isEqualTo(42L);
    }

    @Test
    void rejectsNullAuthentication() {
        assertThatThrownBy(() -> jwtService.generateToken((Authentication) null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
