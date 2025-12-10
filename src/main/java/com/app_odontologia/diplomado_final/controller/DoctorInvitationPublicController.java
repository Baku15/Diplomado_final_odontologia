package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.doctor.DoctorInvitationRegisterRequestDto;
import com.app_odontologia.diplomado_final.dto.doctor.DoctorInvitationStatusDto;
import com.app_odontologia.diplomado_final.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    /**
     * POST /{token}/register
     * Fallback approach: accept generic JSON as Map and convert to DTO to avoid mapping issues.
     * This method logs the raw body (temporary) to help debug mapping issues.
     */
    @PostMapping("/{token}/register")
    public ResponseEntity<Void> registerDoctorFromInvitation(
            @PathVariable String token,
            @RequestBody(required = false) Map<String, Object> rawBody
    ) {
        // Debug: show raw body in logs (use logger in production)
        System.out.println("DEBUG registerDoctorFromInvitation: rawBody = " + rawBody);

        // Extract fields safely
        String firstName = rawBody != null && rawBody.get("firstName") != null ? rawBody.get("firstName").toString() : null;
        String lastName = rawBody != null && rawBody.get("lastName") != null ? rawBody.get("lastName").toString() : null;
        String password = rawBody != null && rawBody.get("password") != null ? rawBody.get("password").toString() : null;
        String username = rawBody != null && rawBody.get("username") != null ? rawBody.get("username").toString() : null;
        String phone = rawBody != null && rawBody.get("phone") != null ? rawBody.get("phone").toString() : null;

        DoctorInvitationRegisterRequestDto dto = new DoctorInvitationRegisterRequestDto();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setPassword(password);
        dto.setUsername(username);
        // phone is optional on DTO, not present in that DTO but staffService may accept phone via another param
        // call service
        staffService.registerDoctorFromInvitation(token, dto);

        return ResponseEntity.ok().build();
    }
}
