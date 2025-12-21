package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.appointment.*;
import com.app_odontologia.diplomado_final.service.AppointmentService;
import com.app_odontologia.diplomado_final.service.ClinicalConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/clinic/{clinicId}/patients/{patientId}/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ClinicalConsultationService consultationService;


    @PostMapping
    public ResponseEntity<AppointmentDto> create(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @RequestParam Long doctorId,
            @RequestBody CreateAppointmentRequest request
    ) {
        return ResponseEntity.ok(
                appointmentService.createAppointment(
                        clinicId,
                        patientId,
                        doctorId,
                        request
                )
        );
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentDto>> agenda(
            @PathVariable Long doctorId,
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(
                appointmentService.listDoctorAgenda(doctorId, date)
        );
    }

    // 🔥 NUEVO: cancelar cita
    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<AppointmentDto> cancel(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.ok(
                appointmentService.cancelAppointment(appointmentId)
        );
    }

    // 🔥 NUEVO: marcar no asistencia
    @PostMapping("/{appointmentId}/no-show")
    public ResponseEntity<AppointmentDto> noShow(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.ok(
                appointmentService.markNoShow(appointmentId)
        );
    }

    @PostMapping("/{appointmentId}/confirm")
    public ResponseEntity<AppointmentDto> confirm(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(
                appointmentService.confirmAttendance(appointmentId)
        );
    }

    @PostMapping("/{appointmentId}/cancel-late")
    public ResponseEntity<AppointmentDto> cancelLate(
            @PathVariable Long appointmentId,
            @RequestParam boolean accepted
    ) {
        return ResponseEntity.ok(
                appointmentService.cancelLate(appointmentId, accepted)
        );
    }

    @PostMapping("/{appointmentId}/complete")
    public ResponseEntity<AppointmentDto> complete(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.ok(
                appointmentService.completeAppointment(appointmentId)
        );
    }

    @PostMapping("/doctor/{doctorId}")
    public ResponseEntity<AppointmentDto> createDirect(
            @PathVariable Long clinicId,
            @PathVariable Long doctorId,
            @RequestBody CreateAppointmentRequest request
    ) {
        return ResponseEntity.ok(
                appointmentService.createDirectAppointment(
                        clinicId,
                        doctorId,
                        request
                )
        );
    }


    @PutMapping("/{appointmentId}")
    public ResponseEntity<AppointmentDto> update(
            @PathVariable Long appointmentId,
            @RequestBody UpdateAppointmentRequest request
    ) {
        return ResponseEntity.ok(
                appointmentService.updateAppointment(appointmentId, request)
        );
    }

    @PostMapping("/{appointmentId}/attend")
    public ResponseEntity<Void> attend(
            @PathVariable Long appointmentId,
            Authentication auth
    ) {
        consultationService.startFromAppointment(
                appointmentId,
                auth.getName()
        );
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/edit/{appointmentId}")
    public ResponseEntity<AppointmentDto> updateFromAgenda(
            @PathVariable Long appointmentId,
            @RequestBody UpdateAppointmentRequest request
    ) {
        return ResponseEntity.ok(
                appointmentService.updateAppointment(appointmentId, request)
        );
    }

    @PostMapping("/{appointmentId}/complete-direct")
    public ResponseEntity<AppointmentDto> completeDirect(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.ok(
                appointmentService.completeDirectAppointment(appointmentId)
        );
    }


}
