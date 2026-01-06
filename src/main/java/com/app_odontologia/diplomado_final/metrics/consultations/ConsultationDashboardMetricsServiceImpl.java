package com.app_odontologia.diplomado_final.metrics.consultations;

import com.app_odontologia.diplomado_final.model.entity.ClinicalConsultation;
import com.app_odontologia.diplomado_final.model.entity.ClinicalConsultation.ConsultationStatus;
import com.app_odontologia.diplomado_final.repository.ClinicalConsultationRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConsultationDashboardMetricsServiceImpl
        implements ConsultationDashboardMetricsService {

    private final ClinicalConsultationRepository consultationRepository;
    private final UserRepository userRepository;

    @Override
    public ConsultationDashboardMetricsDto getDashboardMetrics(
            ConsultationMetricPeriod period,
            Authentication authentication
    ) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getSubject();

        Long dentistId = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Dentista no encontrado"))
                .getId();

        ZoneId zone = ZoneId.of("America/La_Paz");
        ZonedDateTime nowZoned = ZonedDateTime.now(zone);

        ZonedDateTime startZoned = switch (period) {
            case WEEK -> nowZoned.minusDays(7).toLocalDate().atStartOfDay(zone);
            case MONTH -> nowZoned.minusDays(30).toLocalDate().atStartOfDay(zone);
            default -> nowZoned.toLocalDate().atStartOfDay(zone);
        };

        Instant start = startZoned.toInstant();
        Instant end = nowZoned.toInstant();

        // =======================
        // CONTADORES
        // =======================
        long total =
                consultationRepository.countByDentistIdAndStartedAtBetween(
                        dentistId, start, end
                );

        long closed =
                consultationRepository.countByDentistIdAndStatusAndStartedAtBetween(
                        dentistId, ConsultationStatus.CLOSED, start, end
                );

        long inProgress =
                consultationRepository.countByDentistIdAndStatusAndStartedAtBetween(
                        dentistId, ConsultationStatus.IN_PROGRESS, start, end
                );

        // =======================
        // HISTÓRICO
        // =======================
        Map<String, Long> grouped;

        List<Object[]> raw =
                consultationRepository.countClosedGroupedByDate(dentistId, start, end);

        if (period == ConsultationMetricPeriod.MONTH) {
            grouped = groupByWeek(raw, startZoned.toLocalDate());
        } else {
            grouped = new LinkedHashMap<>();
            for (Object[] row : raw) {
                LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                grouped.put(date.toString(), (Long) row[1]);
            }
        }

        // =======================
        // ⏱️ TIEMPOS
        // =======================
        Double avgMinutes =
                consultationRepository.findAverageDurationMinutesByDentistAndPeriod(
                        dentistId, start, end
                );

        List<ClinicalConsultation> closedList =
                consultationRepository.findClosedByDentistAndEndedAtBetween(
                        dentistId, start, end
                );

        long longest = 0;
        long longCount = 0;

        for (ClinicalConsultation c : closedList) {
            if (c.getStartedAt() == null || c.getEndedAt() == null) continue;

            long minutes =
                    Duration.between(c.getStartedAt(), c.getEndedAt()).toMinutes();

            if (minutes <= 0) continue;

            longest = Math.max(longest, minutes);
            if (minutes >= 60) longCount++;
        }

        ConsultationTimeMetricsDto time =
                new ConsultationTimeMetricsDto(
                        avgMinutes == null ? 0 : Math.round(avgMinutes),
                        longest,
                        longCount
                );

        // =======================
        // ALERTAS
        // =======================
        List<ClinicalConsultation> open =
                consultationRepository.findOpenByDentist(dentistId);

        long over2h = 0;
        long over24h = 0;

        for (ClinicalConsultation c : open) {
            long mins = Duration.between(c.getStartedAt(), end).toMinutes();
            if (mins >= 120) over2h++;
            if (mins >= 1440) over24h++;
        }

        return new ConsultationDashboardMetricsDto(
                new ConsultationTodayMetricsDto(
                        total,
                        inProgress,
                        closed,
                        time.averageDurationMinutes(),
                        time.longestDurationMinutes(),
                        null
                ),
                new ConsultationHistoricalMetricsDto(grouped),
                new ConsultationRiskMetricsDto(
                        over2h,
                        over24h,
                        0
                ),
                time
        );

    }

    // ==================================================
    // 🔹 AGRUPACIÓN SEMANAL (MONTH)
    // ==================================================
    private Map<String, Long> groupByWeek(List<Object[]> rows, LocalDate start) {

        Map<Integer, Long> tmp = new TreeMap<>();

        for (Object[] row : rows) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            long count = (Long) row[1];

            long days = ChronoUnit.DAYS.between(start, date);
            int week = (int) (days / 7) + 1;

            tmp.merge(week, count, Long::sum);
        }

        Map<String, Long> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, Long> e : tmp.entrySet()) {
            result.put("Semana " + e.getKey(), e.getValue());
        }

        return result;
    }
}
