package com.app_odontologia.diplomado_final.dto.odontogram.metrics;

import com.app_odontologia.diplomado_final.metrics.odontogram.ProcedureCountDto;
import com.app_odontologia.diplomado_final.metrics.odontogram.ProcedureTimelineItemDto;
import com.app_odontologia.diplomado_final.metrics.odontogram.ToothStatusCountDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OdontogramMetricsDto {

    // Procedimientos
    private long totalProcedures;
    private long openProcedures;
    private long completedProcedures;

    // Por tipo
    private List<ProcedureCountDto> proceduresByType;

    // Dientes
    private long totalTeeth;
    private long affectedTeeth;
    private long healthyTeeth;
    private List<ToothStatusCountDto> teethByStatus;

    // Evidencia
    private long teethWithAttachments;

    // Riesgo
    private long openProceduresOver30Days;

    // Evolución
    private List<ProcedureTimelineItemDto> proceduresTimeline;
}
