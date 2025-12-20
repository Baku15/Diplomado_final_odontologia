package com.app_odontologia.diplomado_final.metrics.patients;

import com.app_odontologia.diplomado_final.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientMetricsServiceImpl implements PatientMetricsService {

    private final PatientRepository patientRepository;

    @Override
    public PatientMetricsDto getPatientMetrics(Long clinicId) {

        long totalPatients =
                patientRepository.countByClinicId(clinicId);

        long activePatients =
                patientRepository.countActivePatients(clinicId);

        // Últimos 12 meses
        List<PatientMetricsDto.MonthlyCount> monthly = new ArrayList<>();

        LocalDate now = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            LocalDate start = now.minusMonths(i).withDayOfMonth(1);
            LocalDate end = start.plusMonths(1);

            Instant from = start.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant to = end.atStartOfDay().toInstant(ZoneOffset.UTC);

            long count =
                    patientRepository.countByClinicIdAndCreatedAtBetween(
                            clinicId, from, to
                    );

            monthly.add(
                    new PatientMetricsDto.MonthlyCount(
                            start.getYear() + "-" + String.format("%02d", start.getMonthValue()),
                            count
                    )
            );
        }

        return new PatientMetricsDto(
                totalPatients,
                activePatients,
                monthly
        );
    }
}
