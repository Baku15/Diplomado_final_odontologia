package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.ApiResponse;
import com.app_odontologia.diplomado_final.dto.ChangePasswordRequestDto;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AccountSecurityController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Permite a un usuario autenticado cambiar su propia contraseña.
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto request,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            // En teoría nunca llega aquí porque SecurityConfig ya devuelve 401
            return ResponseEntity.status(401)
                    .body(new ApiResponse("No autenticado.", false));
        }

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuario no encontrado: " + username)
                );

        // 1) Validar contraseña actual
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("La contraseña actual no es correcta.", false));
        }

        // 2) Evitar misma contraseña
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("La nueva contraseña no puede ser igual a la actual.", false));
        }

        // 3) Actualizar contraseña
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false); // por si venía obligado
        userRepository.save(user);

        return ResponseEntity.ok(
                new ApiResponse("Contraseña actualizada correctamente.", true)
        );
    }
}
