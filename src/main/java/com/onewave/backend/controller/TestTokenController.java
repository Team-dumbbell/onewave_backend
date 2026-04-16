package com.onewave.backend.controller; // 적절한 패키지로 변경

import com.onewave.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestTokenController {

    private final JwtService jwtService;

    @GetMapping("/api/test/token")
    public String getTestToken() {
        // 실제 DB(user 테이블)에 있는 본인의 데이터를 넣으세요.
        // 예시 데이터입니다.
        String googleSub = "101483506090581481270";
        Long internalId = 1L;
        String email = "richard.kim146@gmail.com";

        return jwtService.issueAccessToken(googleSub, internalId, email);
    }
}