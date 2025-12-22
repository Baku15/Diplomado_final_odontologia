package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinic/{clinicId}/appointments")
@RequiredArgsConstructor
public class DirectAppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/{appointmentId}/complete-direct")
    public ResponseEntity<Void> completeDirect(
            @PathVariable Long clinicId,
            @PathVariable Long appointmentId,
            Authentication authentication
    ) {
        appointmentService.completeDirectAppointment(
                clinicId,
                appointmentId,
                authentication.getName()
        );
        return ResponseEntity.noContent().build();
    }
}
