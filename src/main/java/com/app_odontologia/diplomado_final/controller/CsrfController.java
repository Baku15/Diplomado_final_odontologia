package com.app_odontologia.diplomado_final.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CsrfController {

    @GetMapping("/csrf")
    public Map<String, String> csrf(HttpServletRequest request) {
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrf == null) {
            return Map.of();
        }
        return Map.of(
                "parameterName", csrf.getParameterName(),
                "token", csrf.getToken()
        );
    }
}
