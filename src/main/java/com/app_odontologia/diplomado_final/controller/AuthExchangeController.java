package com.app_odontologia.diplomado_final.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthExchangeController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String issuer = "http://localhost:8080";
    private final String clientId = "odontoweb";

    @PostMapping("/exchange")
    public ResponseEntity<?> exchangeCode(
            @RequestParam String code,
            @RequestParam("code_verifier") String codeVerifier,
            HttpSession session
    ) {
        String tokenUrl = issuer + "/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var body = new HashMap<String, String>();
        body.put("grant_type", "authorization_code");
        body.put("code", code);
        body.put("redirect_uri", "http://localhost:4200/callback");
        body.put("client_id", clientId);
        body.put("code_verifier", codeVerifier);

        HttpEntity<Map<String, String>> req = new HttpEntity(body, headers);

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    req,
                    Map.class
            );

            String refresh = (String) resp.getBody().get("refresh_token");
            if (refresh != null) {
                session.setAttribute("refresh_token", refresh);
            }

            return ResponseEntity.ok(resp.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(400).body("token exchange failed");
        }
    }
}
