package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.DentalChart;
import com.app_odontologia.diplomado_final.model.entity.DentalChart.ChartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Odontogramas.
 *
 * NOTA IMPORTANTE:
 * - ChartStatus.ACTIVE  = odontograma vigente (longitudinal)
 * - ChartStatus.CLOSED  = odontograma ARCHIVADO (histórico)
 *
 * El estado NO representa una consulta clínica.
 */
@Repository
public interface DentalChartRepository extends JpaRepository<DentalChart, Long> {

    /**
     * Obtiene el odontograma ACTIVO del paciente en una clínica.
     * Se asume que solo puede existir uno activo por paciente.
     */
    Optional<DentalChart> findByClinicIdAndPatientIdAndStatus(
            Long clinicId,
            Long patientId,
            ChartStatus status
    );

    /**
     * Historial completo de odontogramas del paciente,
     * ordenado por versión descendente.
     */
    List<DentalChart> findByPatientIdOrderByVersionDesc(Long patientId);

    long countByClinicId(Long clinicId);

    long countByClinicIdAndStatus(
            Long clinicId,
            DentalChart.ChartStatus status);
}
