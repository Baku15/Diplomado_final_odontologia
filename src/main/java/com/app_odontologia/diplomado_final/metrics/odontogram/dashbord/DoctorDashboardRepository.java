package com.app_odontologia.diplomado_final.metrics.odontogram.dashbord;

import com.app_odontologia.diplomado_final.model.entity.DentalProcedure;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface DoctorDashboardRepository extends Repository<DentalProcedure, Long> {

    // 1️⃣ Consultas con actividad clínica real
    @Query("""
        SELECT COUNT(DISTINCT c.id)
        FROM ClinicalConsultation c
        WHERE c.dentist.id = :dentistId
          AND c.startedAt BETWEEN :start AND :end
          AND EXISTS (
              SELECT 1
              FROM DentalProcedure p
              WHERE p.chart.patient.id = c.patient.id
                AND p.createdAt BETWEEN c.startedAt AND COALESCE(c.endedAt, :end)
          )
    """)
    long countConsultationsWithActivity(
            @Param("dentistId") Long dentistId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    // 2️⃣ Total de procedimientos
    @Query("""
        SELECT COUNT(p)
        FROM DentalProcedure p
        WHERE p.chart.clinic.id = :clinicId
          AND p.createdAt BETWEEN :start AND :end
    """)
    long countTotalProcedures(
            @Param("clinicId") Long clinicId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    // 3️⃣ Procedimientos completados
    @Query("""
        SELECT COUNT(p)
        FROM DentalProcedure p
        WHERE p.chart.clinic.id = :clinicId
          AND p.status = 'COMPLETED'
          AND p.completedAt BETWEEN :start AND :end
    """)
    long countCompletedProcedures(
            @Param("clinicId") Long clinicId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    // 4️⃣ Procedimientos pendientes
    @Query("""
        SELECT COUNT(p)
        FROM DentalProcedure p
        WHERE p.chart.clinic.id = :clinicId
          AND p.status = 'OPEN'
          AND p.createdAt BETWEEN :start AND :end
    """)
    long countPendingProcedures(
            @Param("clinicId") Long clinicId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    // 5️⃣ Dientes intervenidos
    @Query("""
        SELECT COUNT(DISTINCT p.toothNumber)
        FROM DentalProcedure p
        WHERE p.chart.clinic.id = :clinicId
          AND p.toothNumber IS NOT NULL
          AND p.createdAt BETWEEN :start AND :end
    """)
    long countTeethIntervened(
            @Param("clinicId") Long clinicId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    // 6️⃣ Dientes con ALTA carga clínica
    @Query("""
        SELECT COUNT(DISTINCT p.toothNumber)
        FROM DentalProcedure p
        WHERE p.chart.clinic.id = :clinicId
          AND p.toothNumber IS NOT NULL
          AND p.createdAt BETWEEN :start AND :end
        GROUP BY p.toothNumber
        HAVING COUNT(p) >= :threshold
    """)
    long countTeethWithHighLoad(
            @Param("clinicId") Long clinicId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("threshold") long threshold
    );

    // 7️⃣ Dientes con imágenes
    @Query("""
        SELECT COUNT(DISTINCT ta.tooth.id)
        FROM ToothAttachment ta
        WHERE ta.tooth.chart.clinic.id = :clinicId
          AND ta.createdAt BETWEEN :start AND :end
    """)
    long countTeethWithImages(
            @Param("clinicId") Long clinicId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    // 🔍 Detalle de dientes intervenidos por período
    @Query("""
    SELECT p.toothNumber, COUNT(p)
    FROM DentalProcedure p
    WHERE p.chart.clinic.id = :clinicId
      AND p.toothNumber IS NOT NULL
      AND p.createdAt BETWEEN :start AND :end
    GROUP BY p.toothNumber
    ORDER BY COUNT(p) DESC
""")
    List<Object[]> findTeethInterventionDetail(
            @Param("clinicId") Long clinicId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

}
