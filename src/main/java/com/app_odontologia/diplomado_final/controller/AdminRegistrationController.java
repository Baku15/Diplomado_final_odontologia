package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.ApiResponse;
import com.app_odontologia.diplomado_final.dto.ApproveRegistrationDto;
import com.app_odontologia.diplomado_final.dto.RegistrationRequestViewDto;
import com.app_odontologia.diplomado_final.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // ✅
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/registrations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERUSER')")
public class AdminRegistrationController {
    private final RegistrationService registrationService;

    @GetMapping("/pending")
    public Page<RegistrationRequestViewDto> listPending(@PageableDefault(size=20) Pageable pageable) {
        return registrationService.listPending(pageable);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse> approve(
            @PathVariable Long id,
            @RequestBody ApproveRegistrationDto dto,
            Authentication auth) {
        String adminUsername = auth.getName(); // <-- aquí obtienes el username
        registrationService.approve(id, dto, adminUsername);
        return ResponseEntity.ok(new ApiResponse("Solicitud aprobada y credenciales enviadas."));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse> reject(@PathVariable Long id, Authentication auth) {
        registrationService.reject(id, auth.getName(), "Datos no válidos");
        return ResponseEntity.ok(new ApiResponse("Solicitud rechazada."));
    }
}
