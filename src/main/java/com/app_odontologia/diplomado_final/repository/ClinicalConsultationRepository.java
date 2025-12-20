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

    // ============================
    // 📊 MÉTRICAS
    // ============================

    // 🔹 Total consultas
    long countByClinicId(Long clinicId);

    // 🔹 Consultas por estado
    long countByClinicIdAndStatus(
            Long clinicId,
            ConsultationStatus status
    );

    // 🔹 Consultas por rango de fechas
    long countByClinicIdAndStartedAtBetween(
            Long clinicId,
            Instant from,
            Instant to
    );

    // 🔹 Duración promedio (MINUTOS) — NATIVA (OBLIGATORIO)
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

    // 🔹 Pacientes activos (al menos una consulta)
    @Query("""
        SELECT COUNT(DISTINCT c.patient.id)
        FROM ClinicalConsultation c
        WHERE c.clinic.id = :clinicId
    """)
    long countDistinctPatientIdByClinicId(@Param("clinicId") Long clinicId);

    @Query("""
    SELECT c
    FROM ClinicalConsultation c
    WHERE c.patient.id = :patientId
      AND c.status IN ('ACTIVE', 'IN_PROGRESS')
    ORDER BY c.startedAt DESC
""")
    List<ClinicalConsultation> findOpenConsultations(@Param("patientId") Long patientId);


    @Query("""
    SELECT COUNT(c) > 0
    FROM ClinicalConsultation c
    WHERE c.patient.id = :patientId
      AND c.status IN ('ACTIVE', 'IN_PROGRESS')
""")

    boolean existsOpenConsultations(@Param("patientId") Long patientId);

}
