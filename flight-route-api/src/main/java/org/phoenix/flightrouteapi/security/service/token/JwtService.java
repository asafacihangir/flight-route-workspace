package org.phoenix.flightrouteapi.security.service.token;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.phoenix.flightrouteapi.security.config.properties.JwtProperties;
import org.phoenix.flightrouteapi.security.service.auth.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final String CLAIM_AUTHORITIES = "auth";
    private static final String CLAIM_USER_ID = "userId";

    private final JwtEncoder jwtEncoder;
    private final Duration expiration;

    public JwtService(JwtProperties properties) {
        this.jwtEncoder = buildEncoder(properties.getSecret());
        this.expiration = Duration.ofMinutes(properties.getExpirationMinutes());
    }

    public String generateToken(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("authentication cannot be null");
        }

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        Instant now = Instant.now();
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .claim(CLAIM_AUTHORITIES, authorities)
                .issuedAt(now)
                .expiresAt(now.plus(expiration));

        if (authentication.getPrincipal() instanceof AuthenticatedUser principal) {
            builder.claim(CLAIM_USER_ID, principal.getUserId());
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, builder.build())).getTokenValue();
    }


    public long getExpirationSeconds() {
        return expiration.toSeconds();
    }

    private static JwtEncoder buildEncoder(String secret) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes (256 bits) for HS256");
        }
        SecretKeySpec key = new SecretKeySpec(secretBytes, JWSAlgorithm.HS256.getName());
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }
}
