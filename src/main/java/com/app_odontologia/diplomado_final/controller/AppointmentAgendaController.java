package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.appointment.AppointmentDto;
import com.app_odontologia.diplomado_final.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinic/{clinicId}/patients/{patientId}/appointments")
@RequiredArgsConstructor
public class AppointmentAgendaController {

    private final AppointmentService appointmentService;

    @PostMapping("/{appointmentId}/complete")
    public ResponseEntity<AppointmentDto> completeClinical(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.ok(
                appointmentService.completeClinicalAppointment(
                        clinicId,
                        patientId,
                        appointmentId
                )
        );
    }
}
