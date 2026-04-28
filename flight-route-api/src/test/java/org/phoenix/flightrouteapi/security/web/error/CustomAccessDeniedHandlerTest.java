package org.phoenix.flightrouteapi.security.web.error;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomAccessDeniedHandlerTest {

    private CustomAccessDeniedHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().build();
        handler = new CustomAccessDeniedHandler(objectMapper);
    }

    @Test
    void writesProblemDetailJsonWith403() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestURI()).thenReturn("/api/admin/things");

        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(captured));

        handler.handle(request, response, new AccessDeniedException("forbidden"));

        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response).setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        JsonNode json = objectMapper.readTree(captured.toByteArray());
        assertThat(json.get("status").asInt()).isEqualTo(403);
        assertThat(json.get("title").asString()).isEqualTo("Forbidden");
        assertThat(json.get("detail").asString()).isEqualTo("Access denied");
        assertThat(json.get("instance").asString()).isEqualTo("/api/admin/things");
    }

    private static final class DelegatingServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream delegate;

        DelegatingServletOutputStream(ByteArrayOutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int b) {
            delegate.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }
    }
}
