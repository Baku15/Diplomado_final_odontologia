package com.app_odontologia.diplomado_final.metrics.patients;

import com.app_odontologia.diplomado_final.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientDashboardService {

    private final PatientRepository patientRepository;
    private final ClinicalConsultationRepository consultationRepository;

    public PatientDashboardMetricsDto getMetrics(
            Long clinicId,
            Instant from,
            Instant to
    ) {

        long totalPatients = patientRepository.countByClinicId(clinicId);
        long newPatients =
                patientRepository.countByClinicIdAndCreatedAtBetween(clinicId, from, to);

        long activePatients =
                consultationRepository.countActivePatientsInPeriod(clinicId, from, to);

        long inTreatment =
                consultationRepository.countPatientsInTreatment(clinicId);

        long atRisk =
                patientRepository.countByClinicIdAndRiskLevelNot(
                        clinicId,
                        com.app_odontologia.diplomado_final.model.entity.Patient.RiskLevel.NORMAL
                );

        long inactive = totalPatients - activePatients;

        List<PatientsByDateDto> byDate =
                consultationRepository.countPatientsGroupedByDate(clinicId, from, to)
                        .stream()
                        .map(r -> new PatientsByDateDto(
                                ((java.sql.Date) r[0]).toLocalDate(),
                                ((Number) r[1]).longValue()
                        ))
                        .toList();

        List<TopPatientDto> top =
                consultationRepository.findTopPatients(clinicId, from, to)
                        .stream()
                        .limit(5)
                        .map(r -> new TopPatientDto(
                                (Long) r[0],
                                (String) r[1],
                                ((Number) r[2]).longValue()
                        ))
                        .toList();

        return PatientDashboardMetricsDto.builder()
                .totalPatients(totalPatients)
                .newPatients(newPatients)
                .activePatients(activePatients)
                .patientsInTreatment(inTreatment)
                .patientsAtRisk(atRisk)
                .inactivePatients(inactive)
                .patientsByDate(byDate)
                .topPatients(top)
                .build();
    }

    public List<PatientListItemDto> getPatientsByCategory(
            Long clinicId,
            String category,
            Instant from,
            Instant to
    ) {

        return switch (category) {

            case "Nuevos" ->
                    patientRepository.findNewPatients(clinicId, from, to);

            case "Recurrentes" ->
                    patientRepository.findRecurrentPatients(clinicId, from, to);

            case "Inactivos" ->
                    patientRepository.findInactivePatients(clinicId, from, to);

            default -> List.of();
        };
    }

}
