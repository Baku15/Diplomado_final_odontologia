package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.ActivateAccountRequest;
import com.app_odontologia.diplomado_final.dto.ApiResponse;
import com.app_odontologia.diplomado_final.service.ActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthActivationController {

    private final ActivationService activationService;

    @GetMapping("/activate/validate")
    public ResponseEntity<ApiResponse> validate(@RequestParam String token) {
        return ResponseEntity.ok(new ApiResponse("Token recibido", true));
    }

    @PostMapping("/activate/{token}")
    public ApiResponse activate(@PathVariable String token,
                                @RequestBody ActivateAccountRequest request) {
        activationService.activateAccount(token, request.getNewPassword());
        return new ApiResponse("Cuenta activada correctamente. Ya puedes iniciar sesión.", true);
    }
}