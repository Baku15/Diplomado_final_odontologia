package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.appointment.AppointmentDto;
import com.app_odontologia.diplomado_final.dto.appointment.CreateAppointmentRequest;
import com.app_odontologia.diplomado_final.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

    @RestController
    @RequestMapping("/api/clinic/{clinicId}/appointments")
    @RequiredArgsConstructor
    public class DoctorAgendaController {

        private final AppointmentService appointmentService;

        // 📅 Agenda global del doctor (sin paciente)
        @GetMapping("/doctor/{doctorId}")
        public ResponseEntity<List<AppointmentDto>> agenda(
                @PathVariable Long clinicId,
                @PathVariable Long doctorId,
                @RequestParam LocalDate date
        ) {
            return ResponseEntity.ok(
                    appointmentService.listDoctorAgenda(doctorId, date)
            );
        }

        // 📞 Crear cita DIRECTA (llamada / agenda)
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
    }

