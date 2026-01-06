package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.metrics.appointment.AppointmentDashboardMetricsService;
import com.app_odontologia.diplomado_final.metrics.appointment.AppointmentListItemDto;
import com.app_odontologia.diplomado_final.metrics.appointment.AppointmentMetricPeriod;
import com.app_odontologia.diplomado_final.model.entity.Appointment;
import com.app_odontologia.diplomado_final.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.ZoneId;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/doctor/appointments")
@RequiredArgsConstructor
public class AppointmentDoctorQueryController {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentDashboardMetricsService metricsService;

    @GetMapping
    public Page<AppointmentListItemDto> list(
            @RequestParam AppointmentMetricPeriod period,
            @RequestParam(required = false) Appointment.AppointmentStatus status,
            @RequestParam int page,
            @RequestParam int size,
            Authentication authentication
    ) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long doctorId =
                metricsService.resolveDoctorIdByUsername(jwt.getSubject());


        ZoneId ZONE = ZoneId.of("America/La_Paz");
        LocalDate today = LocalDate.now(ZONE);
        LocalDate start;
        LocalDate end;

        switch (period) {
            case WEEK -> {
                start = today;
                end = today.plusDays( 6);
            }
            case MONTH -> {
                start = today.withDayOfMonth(1);
                end = today.withDayOfMonth(today.lengthOfMonth());
            }
            default -> {
                start = today;
                end = today;
            }
        }

        Pageable pageable =
                PageRequest.of(page, size, Sort.by("startTime").ascending());

        Page<Appointment> result =
                status != null
                        ? appointmentRepository.findByDoctorIdAndStatusAndDateBetween(
                        doctorId, status, start, end, pageable)
                        : appointmentRepository.findByDoctorIdAndDateBetween(
                        doctorId, start, end, pageable);

        return result.map(a ->
                new AppointmentListItemDto(
                        a.getId(),
                        a.getDate(),
                        a.getStartTime(),
                        a.getEndTime(),
                        a.getStatus(),
                        a.getOrigin(),
                        a.getPatient() != null ? a.getPatient().getId() : null,
                        a.getPatient() != null
                                ? a.getPatient().getGivenName() + " " + a.getPatient().getFamilyName()
                                : "Paciente no registrado"
                )
        );
    }
}
