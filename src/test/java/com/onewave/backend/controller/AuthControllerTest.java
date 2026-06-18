package com.onewave.backend.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerTest {

    @Test
    void googleRedirectsToLocalOauth2AuthorizationEndpoint() throws Exception {
        AuthController controller = new AuthController();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.google(response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).isEqualTo("/oauth2/authorization/google");
    }
}
