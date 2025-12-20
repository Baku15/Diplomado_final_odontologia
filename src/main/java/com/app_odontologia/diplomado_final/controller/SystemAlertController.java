package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.model.entity.SystemAlert;
import com.app_odontologia.diplomado_final.service.SystemAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class SystemAlertController {

    private final SystemAlertService alertService;

    @GetMapping
    public List<SystemAlert> getActiveAlerts(
            @RequestParam Long clinicId
    ) {
        return alertService.getActiveAlerts(clinicId);
    }

    @GetMapping("/count")
    public long getAlertCount(
            @RequestParam Long clinicId
    ) {
        return alertService.getActiveAlertCount(clinicId);
    }

    @PostMapping("/{id}/resolve")
    public void resolveAlert(@PathVariable Long id) {
        alertService.resolveAlert(id);
    }
}
