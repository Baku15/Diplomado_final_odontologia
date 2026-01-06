package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.Tooth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToothRepository extends JpaRepository<Tooth, Long> {

    List<Tooth> findByChartId(Long chartId);

    Optional<Tooth> findByChartIdAndToothNumber(Long chartId, Integer toothNumber);

    // 🔹 Total de dientes en un odontograma
    @Query("""
        SELECT COUNT(t)
        FROM Tooth t
        WHERE t.chart.id = :chartId
    """)
    long countTotalTeeth(Long chartId);

    // 🔹 Conteo de dientes por estado
    @Query("""
        SELECT t.toothStatus, COUNT(t)
        FROM Tooth t
        WHERE t.chart.id = :chartId
        GROUP BY t.toothStatus
    """)
    List<Object[]> countTeethByStatus(Long chartId);
}
