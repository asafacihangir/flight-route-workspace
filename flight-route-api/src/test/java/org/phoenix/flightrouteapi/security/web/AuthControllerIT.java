package org.phoenix.flightrouteapi.security.web;

import org.phoenix.flightrouteapi.BaseIT;
import org.phoenix.flightrouteapi.security.domain.Role;
import org.phoenix.flightrouteapi.security.domain.User;
import org.phoenix.flightrouteapi.security.repository.UserRepository;
import org.phoenix.flightrouteapi.security.web.dto.CurrentUserResponse;
import org.phoenix.flightrouteapi.security.web.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.ExchangeResult;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerIT extends BaseIT {

    private static final String USERNAME = "alice";
    private static final String PASSWORD = "correct-horse-battery-staple";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedUser() {
        userRepository.deleteAll();
        userRepository.save(new User(USERNAME, passwordEncoder.encode(PASSWORD), Role.AGENCY));
    }

    @Test
    void loginWithCorrectPasswordReturnsBearerToken() {
        LoginResponse response = restTestClient
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(USERNAME, PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .returnResult(LoginResponse.class)
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.expiresIn()).isPositive();
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorizedAndNoToken() {
        ExchangeResult result = restTestClient
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "definitely-wrong"
                        }
                        """.formatted(USERNAME))
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult();

        byte[] bodyBytes = result.getResponseBodyContent();
        String body = bodyBytes == null ? "" : new String(bodyBytes);
        assertThat(body).doesNotContain("accessToken");
        assertThat(body).doesNotContain("Bearer");
    }

    @Test
    void meWithValidTokenReturnsCurrentUser() {
        String token = obtainAccessToken();

        CurrentUserResponse response = restTestClient
                .get()
                .uri("/api/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .returnResult(CurrentUserResponse.class)
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo(USERNAME);
        assertThat(response.id()).isNotNull();
        assertThat(response.role()).isEqualTo(Role.AGENCY);
        assertThat(response.authorities()).contains("ROLE_AGENCY");
    }

    @Test
    void meWithoutTokenReturnsUnauthorized() {
        ExchangeResult result = restTestClient
                .get()
                .uri("/api/auth/me")
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult();

        byte[] bodyBytes = result.getResponseBodyContent();
        String body = bodyBytes == null ? "" : new String(bodyBytes);
        assertThat(body).doesNotContain("passwordHash");
    }

    @Test
    void meWhenUserDeletedAfterTokenIssuedReturnsUnauthorized() {
        String token = obtainAccessToken();

        userRepository.deleteAll();

        restTestClient
                .get()
                .uri("/api/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void meReflectsRoleChangeAfterTokenIssued() {
        String token = obtainAccessToken();

        User user = userRepository.findByUsername(USERNAME).orElseThrow();
        userRepository.deleteAll();
        userRepository.save(new User(USERNAME, user.getPasswordHash(), Role.ADMIN));

        CurrentUserResponse response = restTestClient
                .get()
                .uri("/api/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .returnResult(CurrentUserResponse.class)
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.role()).isEqualTo(Role.ADMIN);
        assertThat(response.authorities()).contains("ROLE_ADMIN");
    }

    private String obtainAccessToken() {
        LoginResponse response = restTestClient
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(USERNAME, PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .returnResult(LoginResponse.class)
                .getResponseBody();

        assertThat(response).isNotNull();
        return response.accessToken();
    }
}
