package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.DentalProcedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DentalProcedureRepository extends JpaRepository<DentalProcedure, Long> {

    // 🔥 PROCEDIMIENTOS CREADOS EN UNA CONSULTA
    List<DentalProcedure> findByCreatedInConsultationId(Long consultationId);

    // 🔥 (opcional, para el futuro)
    List<DentalProcedure> findByCompletedInConsultationId(Long consultationId);

    long countByChartClinicId(Long clinicId);

    @Query("""
    SELECT p.type, COUNT(p)
    FROM DentalProcedure p
    WHERE p.chart.id = :chartId
    GROUP BY p.type
""")
    List<Object[]> countProceduresByType(@Param("chartId") Long chartId);

    @Query("""
    SELECT p.status, COUNT(p)
    FROM DentalProcedure p
    WHERE p.chart.id = :chartId
    GROUP BY p.status
""")
    List<Object[]> countProceduresByStatus(@Param("chartId") Long chartId);


    @Query("""
    SELECT t.toothStatus, COUNT(t)
    FROM Tooth t
    WHERE t.chart.id = :chartId
    GROUP BY t.toothStatus
""")
    List<Object[]> countTeethByStatus(@Param("chartId") Long chartId);


    @Query("""
    SELECT COUNT(DISTINCT p.toothNumber)
    FROM DentalProcedure p
    WHERE p.chart.id = :chartId
""")
    Long countAffectedTeeth(@Param("chartId") Long chartId);


    @Query("""
    SELECT COUNT(t)
    FROM Tooth t
    WHERE t.chart.id = :chartId
""")
    Long countTotalTeeth(@Param("chartId") Long chartId);


    @Query("""
    SELECT FUNCTION('date', p.createdAt), COUNT(p)
    FROM DentalProcedure p
    WHERE p.chart.id = :chartId
    GROUP BY FUNCTION('date', p.createdAt)
    ORDER BY FUNCTION('date', p.createdAt)
""")
    List<Object[]> countProceduresGroupedByDate(
            @Param("chartId") Long chartId
    );


    @Query("""
    SELECT COUNT(p)
    FROM DentalProcedure p
    WHERE p.chart.id = :chartId
      AND p.status = 'OPEN'
      AND p.createdAt < :limitDate
""")
    Long countOpenProceduresOlderThan(
            @Param("chartId") Long chartId,
            @Param("limitDate") Instant limitDate
    );

}
