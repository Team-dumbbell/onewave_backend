package com.onewave.backend.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class AuthController {

    @GetMapping("/auth/google")
    public void google(HttpServletResponse response) throws IOException {
        response.sendRedirect("https://team-moleback.store/oauth2/authorization/google");
    }

}