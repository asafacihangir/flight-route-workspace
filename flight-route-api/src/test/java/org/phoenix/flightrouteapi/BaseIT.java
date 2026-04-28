package org.phoenix.flightrouteapi;

import org.phoenix.flightrouteapi.security.domain.Role;
import org.phoenix.flightrouteapi.security.domain.User;
import org.phoenix.flightrouteapi.security.repository.UserRepository;
import org.phoenix.flightrouteapi.security.web.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.RestTestClient;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfig.class)
@AutoConfigureRestTestClient
public abstract class BaseIT {

    protected static final String TEST_PASSWORD = "correct-horse-battery-staple";

    @Autowired
    protected RestTestClient restTestClient;

    @Autowired
    protected JsonMapper jsonMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    protected String seedUserAndLogin(String username, Role role) {
        userRepository.findByUsername(username).ifPresent(u -> userRepository.deleteById(u.getId()));
        userRepository.save(new User(username, passwordEncoder.encode(TEST_PASSWORD), role));

        LoginResponse response = restTestClient
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, TEST_PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .returnResult(LoginResponse.class)
                .getResponseBody();

        return response.accessToken();
    }
}
