package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.DoctorInvitationRegisterRequestDto;
import com.app_odontologia.diplomado_final.dto.DoctorInvitationStatusDto;
import com.app_odontologia.diplomado_final.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/doctor-invitations")
@RequiredArgsConstructor
public class DoctorInvitationPublicController {

    private final StaffService staffService;

    // GET /api/public/doctor-invitations/{token}
    @GetMapping("/{token}")
    public ResponseEntity<DoctorInvitationStatusDto> getInvitationStatus(
            @PathVariable String token
    ) {
        DoctorInvitationStatusDto dto = staffService.getDoctorInvitationStatus(token);
        return ResponseEntity.ok(dto);
    }

    // ⬇️ NUEVO: POST /api/public/doctor-invitations/{token}/register
    @PostMapping("/{token}/register")
    public ResponseEntity<Void> registerDoctorFromInvitation(
            @PathVariable String token,
            @Valid @RequestBody DoctorInvitationRegisterRequestDto request
    ) {
        staffService.registerDoctorFromInvitation(token, request);
        return ResponseEntity.ok().build();
    }
}
