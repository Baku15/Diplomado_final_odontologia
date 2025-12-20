package com.app_odontologia.diplomado_final.metrics.consultations;

import com.app_odontologia.diplomado_final.model.entity.ClinicalConsultation.ConsultationStatus;
import com.app_odontologia.diplomado_final.repository.ClinicalConsultationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationMetricsServiceImpl
        implements ConsultationMetricsService {

    private final ClinicalConsultationRepository repository;

    @Override
    public ConsultationMetricsDto getConsultationMetrics(Long clinicId) {

        long total =
                repository.countByClinicId(clinicId);

        long active =
                repository.countByClinicIdAndStatus(
                        clinicId,
                        ConsultationStatus.ACTIVE
                );

        long closed =
                repository.countByClinicIdAndStatus(
                        clinicId,
                        ConsultationStatus.CLOSED
                );

        Double avgDuration =
                repository.findAverageDurationMinutes(clinicId);

        // Últimos 12 meses
        List<ConsultationMetricsDto.MonthlyCount> monthly = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            LocalDate start = now.minusMonths(i).withDayOfMonth(1);
            LocalDate end = start.plusMonths(1);

            Instant from = start.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant to = end.atStartOfDay().toInstant(ZoneOffset.UTC);

            long count =
                    repository.countByClinicIdAndStartedAtBetween(
                            clinicId, from, to
                    );

            monthly.add(
                    new ConsultationMetricsDto.MonthlyCount(
                            start.getYear() + "-" +
                                    String.format("%02d", start.getMonthValue()),
                            count
                    )
            );
        }

        return new ConsultationMetricsDto(
                total,
                active,
                closed,
                avgDuration,
                monthly
        );
    }
}
