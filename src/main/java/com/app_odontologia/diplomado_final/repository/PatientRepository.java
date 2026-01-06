package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.metrics.patients.PatientListItemDto;
import com.app_odontologia.diplomado_final.model.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    // =========================
    // CRUD BÁSICO
    // =========================

    List<Patient> findByClinicIdAndActiveTrueOrderByFamilyNameAscGivenNameAsc(Long clinicId);

    Optional<Patient> findByIdAndClinicIdAndActiveTrue(Long id, Long clinicId);

    boolean existsByClinicIdAndDocumentTypeAndDocumentNumberAndActiveTrue(
            Long clinicId,
            String documentType,
            String documentNumber
    );

    boolean existsByClinicIdAndEmailIgnoreCaseAndActiveTrue(Long clinicId, String email);

    boolean existsByClinicIdAndPhoneMobileAndActiveTrue(Long clinicId, String phoneMobile);

    boolean existsByClinicIdAndUsernameAndActiveTrue(Long clinicId, String username);

    Optional<Patient> findByClinicIdAndEmailIgnoreCase(Long clinicId, String email);

    Page<Patient> findByClinicId(Long clinicId, Pageable pageable);

    Optional<Patient> findByIdAndClinicId(Long id, Long clinicId);

    // =========================
    // MÉTRICAS GENERALES
    // =========================

    // Total pacientes por clínica
    long countByClinicId(Long clinicId);

    // Pacientes registrados en un rango
    long countByClinicIdAndCreatedAtBetween(
            Long clinicId,
            Instant from,
            Instant to
    );

    // Pacientes activos (con al menos una consulta)
    @Query("""
        SELECT COUNT(DISTINCT c.patient.id)
        FROM ClinicalConsultation c
        WHERE c.clinic.id = :clinicId
    """)
    long countActivePatients(@Param("clinicId") Long clinicId);

    // Pacientes con riesgo distinto a NORMAL
    long countByClinicIdAndRiskLevelNot(
            Long clinicId,
            Patient.RiskLevel riskLevel
    );

    // =========================
    // LISTADOS PARA DASHBOARD
    // =========================

    // 🟢 Pacientes NUEVOS
    @Query("""
        SELECT new com.app_odontologia.diplomado_final.metrics.patients.PatientListItemDto(
            p.id,
            CONCAT(p.givenName, ' ', p.familyName),
            CAST(p.riskLevel AS string),
            CAST(COUNT(c) AS long)
                             )
        FROM Patient p
        JOIN ClinicalConsultation c ON c.patient.id = p.id
        WHERE p.clinic.id = :clinicId
          AND p.createdAt BETWEEN :from AND :to
        GROUP BY p.id, p.givenName, p.familyName, p.riskLevel
    """)
    List<PatientListItemDto> findNewPatients(
            @Param("clinicId") Long clinicId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    // 🔵 Pacientes RECURRENTES
    @Query("""
        SELECT new com.app_odontologia.diplomado_final.metrics.patients.PatientListItemDto(
            p.id,
            CONCAT(p.givenName, ' ', p.familyName),
            CAST(p.riskLevel AS string),
            CAST(COUNT(c) AS long)
                             )
        FROM Patient p
        JOIN ClinicalConsultation c ON c.patient.id = p.id
        WHERE p.clinic.id = :clinicId
          AND p.createdAt < :from
          AND c.startedAt BETWEEN :from AND :to
        GROUP BY p.id, p.givenName, p.familyName, p.riskLevel
    """)
    List<PatientListItemDto> findRecurrentPatients(
            @Param("clinicId") Long clinicId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    // ⚪ Pacientes INACTIVOS
    @Query("""
        SELECT new com.app_odontologia.diplomado_final.metrics.patients.PatientListItemDto(
            p.id,
            CONCAT(p.givenName, ' ', p.familyName),
            CAST(p.riskLevel AS string),
            CAST(0 AS long)
        )
        FROM Patient p
        WHERE p.clinic.id = :clinicId
          AND p.id NOT IN (
              SELECT DISTINCT c.patient.id
              FROM ClinicalConsultation c
              WHERE c.startedAt BETWEEN :from AND :to
          )
    """)
    List<PatientListItemDto> findInactivePatients(
            @Param("clinicId") Long clinicId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
