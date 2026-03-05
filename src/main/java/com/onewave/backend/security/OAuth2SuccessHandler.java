package com.onewave.backend.security;

import com.onewave.backend.domain.user.UserRepository;
import com.onewave.backend.domain.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Value("${app.frontend-auth-callback-path}")
    private String callbackPath;

    public OAuth2SuccessHandler(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
        OAuth2User principal = oauth.getPrincipal();

        String sub = asString(principal.getAttribute("sub"));
        String email = asString(principal.getAttribute("email"));
        String name = asString(principal.getAttribute("name"));

        if (sub == null || sub.isBlank()) {
            response.sendError(500, "Missing Google sub");
            return;
        }
        if (email == null) email = "";

        // upsert by googleSub
        boolean isNewUser = false;
        User user = userRepository.findByGoogleSub(sub).orElse(null);
        if (user == null) {
            isNewUser = true;
            user = User.create(email, sub, name);
        } else {
            user.updateEmail(email);
            user.updateDisplayName(name);
        }
        user = userRepository.save(user);

        String jwt = jwtService.issueAccessToken(sub, user.getId(), user.getEmail());

        String redirectUrl = frontendBaseUrl + callbackPath
                + "#token=" + url(jwt)
                + "&internal_id=" + url(String.valueOf(user.getId()))
                + "&is_new_user=" + (isNewUser ? "1" : "0");

        response.sendRedirect(redirectUrl);
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static String url(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }
}