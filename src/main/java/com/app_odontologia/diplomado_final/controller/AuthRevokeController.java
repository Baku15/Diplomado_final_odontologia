package com.app_odontologia.diplomado_final.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/auth")
public class AuthRevokeController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.oauth.backend.client-id}")
    private String backendClientId;

    @Value("${app.oauth.backend.client-secret}")
    private String backendClientSecret;

    private final String issuer = "http://localhost:8080";

    @PostMapping("/revoke")
    public ResponseEntity<?> revoke(HttpServletRequest request, HttpServletResponse response) {
        try {
            // invalida SecurityContext + JSESSIONID
            new SecurityContextLogoutHandler().logout(request, response, null);

            return ResponseEntity.ok().body("{\"message\": \"revoked\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"logout_failed\"}");
        }
    }
}
