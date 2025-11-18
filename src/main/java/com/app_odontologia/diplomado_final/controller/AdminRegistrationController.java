package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.ApiResponse;
import com.app_odontologia.diplomado_final.dto.ApproveRegistrationDto;
import com.app_odontologia.diplomado_final.dto.RegistrationRequestViewDto;
import com.app_odontologia.diplomado_final.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/registration-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERUSER')")
public class AdminRegistrationController {

    private final RegistrationService registrationService;

    // GET /api/admin/registration-requests/pending?page=0&size=20
    @GetMapping("/pending")
    public Page<RegistrationRequestViewDto> listPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return registrationService.listPending(PageRequest.of(page, size));
    }

    // POST /api/admin/registration-requests/{id}/approve
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse> approve(
            @PathVariable Long id,
            @RequestBody ApproveRegistrationDto dto,
            Authentication auth) {

        String adminUsername = auth.getName();
        registrationService.approve(id, dto, adminUsername);

        return ResponseEntity.ok(
                new ApiResponse("Solicitud aprobada. Se envió el correo de activación.", true)
        );
    }

    // POST /api/admin/registration-requests/{id}/reject
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse> reject(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Datos no válidos") String reason,
            Authentication auth) {

        String adminUsername = auth.getName();
        registrationService.reject(id, adminUsername, reason);

        return ResponseEntity.ok(new ApiResponse("Solicitud rechazada.", true));
    }
}
