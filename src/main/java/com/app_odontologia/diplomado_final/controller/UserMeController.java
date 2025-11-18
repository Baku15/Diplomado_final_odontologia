package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserMeController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        // 1) Identidad del token (subject). Con OIDC típico es el username o user-id que pusiste como "sub"
        String sub = Optional.ofNullable(jwt.getSubject()).orElse("");

        // 2) Busca el usuario en tu BD por username (o por email, si así emites el sub)
        //    Intentamos por username y si no, por email:
        Optional<User> optUser = userRepository.findByUsername(sub);
        if (optUser.isEmpty()) {
            optUser = userRepository.findByEmail(sub);
        }

        List<String> rolesFromDb = optUser
                .map(u -> u.getRoles()
                        .stream()
                        .map(r -> r.getName())            // "ROLE_SUPERUSER", ...
                        .collect(Collectors.toList()))
                .orElseGet(List::of);

        // 3) Arma respuesta "null-safe" (sin Map.of para evitar NPE con nulls)
        Map<String, Object> body = new HashMap<>();
        body.put("sub", sub);
        body.put("username", optUser.map(User::getUsername).orElse(sub));
        body.put("email", optUser.map(User::getEmail).orElse(null));
        body.put("roles", rolesFromDb);

        // (Opcional) si luego agregas clinic al User, puedes incluirlo así:
        // if (optUser.isPresent() && optUser.get().getClinic() != null) {
        //     body.put("clinic_id", optUser.get().getClinic().getId());
        // }

        return body;
    }
}
