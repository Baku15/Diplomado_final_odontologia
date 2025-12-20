package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.odontogram.*;

import java.util.List;

/**
 * Servicio de gestión del Odontograma.
 *
 * IMPORTANTE:
 * - El DentalChart representa el estado dental longitudinal del paciente.
 * - NO representa una consulta o sesión clínica.
 * - El método closeChart() ARCHIVA el odontograma,
 *   no debe usarse para cerrar consultas.
 */
public interface DentalChartService {

    /**
     * Obtiene el odontograma ACTIVO del paciente.
     * ACTIVO = odontograma vigente (no archivado).
     */
    DentalChartDto getActiveChart(Long clinicId, Long patientId);

    /**
     * Crea un odontograma si no existe uno activo.
     * Si ya existe uno ACTIVO, se devuelve ese mismo.
     */
    DentalChartDto createChart(
            Long clinicId,
            Long patientId,
            Long clinicalRecordId,
            String username
    );

    /**
     * Inserta o actualiza un diente dentro del odontograma.
     * El odontograma NO se bloquea por sesiones.
     */
    DentalChartDto upsertTooth(Long chartId, UpsertToothRequest req, String username);

    /**
     * Añade un procedimiento al odontograma.
     * El procedimiento pertenece al chart (y más adelante a una consulta).
     */
    DentalProcedureDto addProcedure(Long chartId, AddProcedureRequest req);

    /**
     * Obtiene el historial de odontogramas del paciente
     * (incluye archivados).
     */
    List<DentalChartDto> getChartHistory(Long patientId);

    /**
     * Archiva el odontograma.
     * NO cerrar consultas con este método.
     */
    DentalChartDto closeChart(Long chartId);

    /**
     * Marca un procedimiento como completado.
     */
    DentalProcedureDto completeProcedure(Long chartId, Long procedureId, String username);

    /**
     * Actualiza un procedimiento existente (si no está completado).
     */
    DentalProcedureDto updateProcedure(Long procedureId, AddProcedureRequest req, String username);

    // ==========================
    // Attachments por diente
    // ==========================

    ToothAttachmentDto addToothAttachment(
            Long chartId,
            Integer toothNumber,
            Long attachmentId,
            String username
    );

    void removeToothAttachment(
            Long chartId,
            Integer toothNumber,
            Long attachmentId,
            String username
    );

    List<DentalProcedureDto> listProceduresByConsultation(Long consultationId);


    List<ToothAttachmentDto> listToothAttachments(Long chartId, Integer toothNumber);
}
