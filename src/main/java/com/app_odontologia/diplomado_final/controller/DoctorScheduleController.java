// src/main/java/com/app_odontologia/diplomado_final/controller/DoctorScheduleController.java
package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.DoctorWeeklyScheduleDto;
import com.app_odontologia.diplomado_final.service.DoctorScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    public DoctorScheduleController(DoctorScheduleService doctorScheduleService) {
        this.doctorScheduleService = doctorScheduleService;
    }

    /**
     * Devuelve el horario semanal del doctor actualmente autenticado.
     */
    @GetMapping("/me/schedule")
    @PreAuthorize("hasAnyAuthority('ROLE_DENTIST','ROLE_CLINIC_ADMIN')")
    public DoctorWeeklyScheduleDto getMySchedule(Authentication authentication) {
        String username = authentication.getName();
        return doctorScheduleService.getMyWeeklySchedule(username);
    }

    /**
     * Guarda/reemplaza el horario semanal del doctor actualmente autenticado.
     */
    @PutMapping("/me/schedule")
    @PreAuthorize("hasAnyAuthority('ROLE_DENTIST','ROLE_CLINIC_ADMIN')")
    public ResponseEntity<Void> saveMySchedule(
            @Valid @RequestBody DoctorWeeklyScheduleDto weeklyDto,
            Authentication authentication) {

        String username = authentication.getName();
        doctorScheduleService.saveMyWeeklySchedule(username, weeklyDto);
        return ResponseEntity.noContent().build();
    }
}
