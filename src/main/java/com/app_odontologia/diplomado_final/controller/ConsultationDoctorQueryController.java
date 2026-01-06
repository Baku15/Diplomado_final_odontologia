package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.metrics.consultations.ConsultationListItemDto;
import com.app_odontologia.diplomado_final.metrics.consultations.ConsultationMetricPeriod;
import com.app_odontologia.diplomado_final.model.entity.ClinicalConsultation;
import com.app_odontologia.diplomado_final.model.entity.ClinicalConsultation.ConsultationStatus;
import com.app_odontologia.diplomado_final.repository.ClinicalConsultationRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/dashboard/doctor/consultations")
@RequiredArgsConstructor
public class ConsultationDoctorQueryController {

    private final ClinicalConsultationRepository repository;
    private final UserRepository userRepository;

    @GetMapping("/list")
    public Page<ConsultationListItemDto> list(
            @RequestParam ConsultationMetricPeriod period,
            @RequestParam(required = false) ConsultationStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam int page,
            @RequestParam int size,
            Authentication authentication
    ) {

        // ===============================
        // 1️⃣ Obtener dentista autenticado
        // ===============================
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getSubject();

        Long dentistId = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Dentista no encontrado"))
                .getId();

        // ===============================
        // 2️⃣ Calcular rango de fechas
        // ===============================
        ZoneId zone = ZoneId.of("America/La_Paz");

        Instant start;
        Instant end;

        if (from != null && to != null) {
            // 👉 Rango explícito (ej: semana seleccionada desde el frontend)
            start = from.atStartOfDay(zone).toInstant();
            end = to.plusDays(1).atStartOfDay(zone).toInstant(); // inclusivo
        } else {
            // 👉 Rango automático según período
            Instant now = Instant.now();
            end = now;

            start = switch (period) {
                case WEEK -> now.minus(7, ChronoUnit.DAYS);
                case MONTH -> now.minus(30, ChronoUnit.DAYS);
                default -> now.minus(1, ChronoUnit.DAYS);
            };
        }

        // ===============================
        // 3️⃣ Pageable
        // ===============================
        PageRequest pageable =
                PageRequest.of(page, size, Sort.by("startedAt").descending());

        // ===============================
        // 4️⃣ CONSULTA CORRECTA SEGÚN ESTADO
        // ===============================
        Page<ClinicalConsultation> result;

        if (status == ConsultationStatus.CLOSED) {
            // 🔥 CLAVE:
            // Para consultas FINALIZADAS usamos endedAt
            result = repository.findByDentistIdAndStatusAndEndedAtBetween(
                    dentistId,
                    ConsultationStatus.CLOSED,
                    start,
                    end,
                    pageable
            );
        } else {
            // Para ACTIVE / IN_PROGRESS usamos startedAt
            result = repository.findForDashboard(
                    dentistId,
                    start,
                    end,
                    status,
                    pageable
            );
        }

        // ===============================
        // 5️⃣ MAPEO A DTO
        // ===============================
        return result.map(c -> new ConsultationListItemDto(
                c.getId(),
                c.getPatient().getId(),
                c.getPatient().getGivenName() + " " + c.getPatient().getFamilyName(),
                c.getStartedAt(),
                c.getEndedAt(),
                c.getStatus().name()
        ));
    }
}
