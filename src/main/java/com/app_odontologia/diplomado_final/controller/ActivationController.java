package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.ActivateAccountRequest;
import com.app_odontologia.diplomado_final.repository.ActivationTokenRepository;
import com.app_odontologia.diplomado_final.service.ActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class ActivationController {

    private final ActivationService activationService;
    private final ActivationTokenRepository activationTokenRepository;

    // 1) GET para que el front verifique si el token es válido (para mostrar la pantalla correcta)
    @GetMapping
    public ResponseEntity<?> check(@RequestParam("token") String token) {
        var opt = activationTokenRepository.findByToken(token);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("ok", false, "message", "Token inválido"));
        }

        var t = opt.get();
        if (t.isUsed()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("ok", false, "message", "Este enlace ya fue utilizado"));
        }
        if (t.getExpiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("ok", false, "message", "Este enlace ha expirado"));
        }

        return ResponseEntity.ok(Map.of("ok", true));
    }

    // 2) POST para activar (guardar la contraseña)
    @PostMapping
    public ResponseEntity<?> activate(@RequestBody ActivateAccountRequest request) {

        activationService.activateAccount(
                request.getToken(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "message", "Cuenta activada correctamente"
        ));
    }}