// src/main/java/com/app_odontologia/diplomado_final/repository/ClinicalConsultationRepository.java
package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.ClinicalConsultation;
import com.app_odontologia.diplomado_final.model.entity.ClinicalConsultation.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ClinicalConsultationRepository
        extends JpaRepository<ClinicalConsultation, Long> {

    Optional<ClinicalConsultation> findByClinicIdAndPatientIdAndStatus(
            Long clinicId,
            Long patientId,
            ConsultationStatus status
    );

    @Query("""
        SELECT c
        FROM ClinicalConsultation c
        WHERE c.clinic.id = :clinicId
          AND c.patient.id = :patientId
          AND c.status <> 'CLOSED'
        ORDER BY c.startedAt DESC
    """)
    Optional<ClinicalConsultation> findOpenConsultation(
            @Param("clinicId") Long clinicId,
            @Param("patientId") Long patientId
    );

    List<ClinicalConsultation> findByClinicIdAndPatientIdOrderByStartedAtDesc(
            Long clinicId,
            Long patientId
    );

    Page<ClinicalConsultation> findByClinicIdAndPatientId(
            Long clinicId,
            Long patientId,
            Pageable pageable
    );

    Page<ClinicalConsultation> findByClinicIdAndPatientIdAndStartedAtBetween(
            Long clinicId,
            Long patientId,
            Instant from,
            Instant to,
            Pageable pageable
    );

    Page<ClinicalConsultation> findByClinicIdAndPatientIdAndStatus(
            Long clinicId,
            Long patientId,
            ConsultationStatus status,
            Pageable pageable
    );

    long countByClinicId(Long clinicId);

    long countByClinicIdAndStatus(
            Long clinicId,
            ConsultationStatus status
    );

    long countByClinicIdAndStartedAtBetween(
            Long clinicId,
            Instant from,
            Instant to
    );

    @Query(
            value = """
                SELECT AVG(EXTRACT(EPOCH FROM (ended_at - started_at)) / 60)
                FROM clinical_consultations
                WHERE clinic_id = :clinicId
                  AND status = 'CLOSED'
                  AND ended_at IS NOT NULL
            """,
            nativeQuery = true
    )
    Double findAverageDurationMinutes(@Param("clinicId") Long clinicId);

    @Query("""
        SELECT COUNT(DISTINCT c.patient.id)
        FROM ClinicalConsultation c
        WHERE c.clinic.id = :clinicId
    """)
    long countDistinctPatientIdByClinicId(@Param("clinicId") Long clinicId);

    long countByDentistIdAndStartedAtBetween(
            Long dentistId,
            Instant start,
            Instant end
    );

    long countByDentistIdAndStatusAndStartedAtBetween(
            Long dentistId,
            ConsultationStatus status,
            Instant start,
            Instant end
    );

    @Query("""
        SELECT FUNCTION('date', c.endedAt), COUNT(c)
        FROM ClinicalConsultation c
        WHERE c.dentist.id = :dentistId
          AND c.status = 'CLOSED'
          AND c.endedAt BETWEEN :start AND :end
        GROUP BY FUNCTION('date', c.endedAt)
        ORDER BY FUNCTION('date', c.endedAt)
    """)
    List<Object[]> countClosedGroupedByDate(
            @Param("dentistId") Long dentistId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    Page<ClinicalConsultation> findByDentistIdAndStartedAtBetween(
            Long dentistId,
            Instant start,
            Instant end,
            Pageable pageable
    );

    Page<ClinicalConsultation> findByDentistIdAndStatusAndStartedAtBetween(
            Long dentistId,
            ConsultationStatus status,
            Instant start,
            Instant end,
            Pageable pageable
    );

    @Query("""
        SELECT c
        FROM ClinicalConsultation c
        WHERE c.dentist.id = :dentistId
          AND c.startedAt BETWEEN :start AND :end
          AND (:status IS NULL OR c.status = :status)
    """)
    Page<ClinicalConsultation> findForDashboard(
            @Param("dentistId") Long dentistId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("status") ConsultationStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT c
        FROM ClinicalConsultation c
        WHERE c.dentist.id = :dentistId
          AND c.status IN ('ACTIVE', 'IN_PROGRESS')
    """)
    List<ClinicalConsultation> findOpenByDentist(
            @Param("dentistId") Long dentistId
    );

    @Query(
            value = """
                SELECT AVG(EXTRACT(EPOCH FROM (ended_at - started_at)) / 60)
                FROM clinical_consultations
                WHERE dentist_id = :dentistId
                  AND status = 'CLOSED'
                  AND started_at BETWEEN :start AND :end
                  AND ended_at IS NOT NULL
            """,
            nativeQuery = true
    )
    Double findAverageDurationMinutesByDentistAndPeriod(
            @Param("dentistId") Long dentistId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("""
        SELECT c
        FROM ClinicalConsultation c
        WHERE c.dentist.id = :dentistId
          AND c.status = 'CLOSED'
          AND c.endedAt BETWEEN :start AND :end
    """)
    List<ClinicalConsultation> findClosedByDentistAndEndedAtBetween(
            @Param("dentistId") Long dentistId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    List<ClinicalConsultation> findByDentistIdAndStatus(
            Long dentistId,
            ConsultationStatus status
    );

    @Query("""
        SELECT c
        FROM ClinicalConsultation c
        WHERE c.patient.id = :patientId
          AND c.status IN ('ACTIVE', 'IN_PROGRESS')
    """)
    List<ClinicalConsultation> findOpenConsultations(
            @Param("patientId") Long patientId
    );

    Page<ClinicalConsultation> findByDentistIdAndStatusAndEndedAtBetween(
            Long dentistId,
            ConsultationStatus status,
            Instant start,
            Instant end,
            Pageable pageable
    );

    // Pacientes activos en período
    @Query("""
    SELECT COUNT(DISTINCT c.patient.id)
    FROM ClinicalConsultation c
    WHERE c.clinic.id = :clinicId
      AND c.startedAt BETWEEN :from AND :to
""")
    long countActivePatientsInPeriod(
            @Param("clinicId") Long clinicId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    // Pacientes en tratamiento
    @Query("""
    SELECT COUNT(DISTINCT c.patient.id)
    FROM ClinicalConsultation c
    WHERE c.clinic.id = :clinicId
      AND c.status IN ('ACTIVE', 'IN_PROGRESS')
""")
    long countPatientsInTreatment(
            @Param("clinicId") Long clinicId
    );

    // Pacientes por día
    @Query("""
    SELECT FUNCTION('date', c.startedAt), COUNT(DISTINCT c.patient.id)
    FROM ClinicalConsultation c
    WHERE c.clinic.id = :clinicId
      AND c.startedAt BETWEEN :from AND :to
    GROUP BY FUNCTION('date', c.startedAt)
    ORDER BY FUNCTION('date', c.startedAt)
""")
    List<Object[]> countPatientsGroupedByDate(
            @Param("clinicId") Long clinicId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    // Top pacientes por consultas
    @Query("""
    SELECT c.patient.id, c.patient.fullNameNorm, COUNT(c)
    FROM ClinicalConsultation c
    WHERE c.clinic.id = :clinicId
      AND c.startedAt BETWEEN :from AND :to
    GROUP BY c.patient.id, c.patient.fullNameNorm
    ORDER BY COUNT(c) DESC
""")
    List<Object[]> findTopPatients(
            @Param("clinicId") Long clinicId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );




}
