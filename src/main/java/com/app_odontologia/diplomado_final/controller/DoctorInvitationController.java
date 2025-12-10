package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.doctor.DoctorInvitationDto;
import com.app_odontologia.diplomado_final.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor-invitations")
@RequiredArgsConstructor
public class DoctorInvitationController {

    private final StaffService staffService;

    /**
     * GET /api/doctor-invitations/{token}
     * Devuelve los datos básicos de la invitación para mostrar en la pantalla
     * de "aceptar invitación".
     */
    @GetMapping("/{token}")
    public ResponseEntity<DoctorInvitationDto> getInvitation(@PathVariable String token) {
        DoctorInvitationDto dto = staffService.getInvitationByToken(token);
        return ResponseEntity.ok(dto);
    }
}
