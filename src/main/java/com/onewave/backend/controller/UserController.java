package com.onewave.backend.controller;

import com.onewave.backend.domain.user.UserRepository;
import com.onewave.backend.domain.user.entity.User;
import com.onewave.backend.exception.ApiResponse;
import com.onewave.backend.exception.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> profile(Authentication authentication) {
        String googleSub = authentication.getName();

        User user = userRepository.findByGoogleSub(googleSub)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        String displayName = user.getDisplayName() != null ? user.getDisplayName() : "HUM User";
        String email = user.getEmail();

        Map<String, Object> data = Map.of(
                "users", Map.of(
                        "display_name", displayName,
                        "email", email
                ),
                "data", Map.of(
                        "settings", Map.of(
                                "level", "Beginner",
                                "streak_days", 0,
                                "favorite_language", "ENGLISH"
                        )
                )
        );

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/words")
    public ResponseEntity<ApiResponse<Map<String, Object>>> words() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("user_words", List.of())));
    }
}
