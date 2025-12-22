package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.SystemAlertDto;
import com.app_odontologia.diplomado_final.model.entity.SystemAlert;
import com.app_odontologia.diplomado_final.service.SystemAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class SystemAlertController {

    private final SystemAlertService alertService;

    @GetMapping
    public List<SystemAlertDto> getActiveAlerts(Authentication authentication) {
        Long clinicId = extractClinicId(authentication);
        return alertService.getActiveAlerts(clinicId);
    }


    @GetMapping("/count")
    public long getAlertCount(Authentication authentication) {
        Long clinicId = extractClinicId(authentication);
        return alertService.getActiveAlertCount(clinicId);
    }

    @PostMapping("/{id}/resolve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resolveAlert(@PathVariable Long id) {
        alertService.resolveAlert(id);
    }


    // 🔒 clinicId SIEMPRE desde JWT
    private Long extractClinicId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            Long clinicId = jwt.getClaim("clinic_id");
            if (clinicId == null) {
                throw new IllegalStateException("clinic_id no presente en el token");
            }
            return clinicId;
        }
        throw new IllegalStateException("Autenticación no soportada");
    }
}
