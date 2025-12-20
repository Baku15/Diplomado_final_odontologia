package com.app_odontologia.diplomado_final.metrics.odontogram;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OdontogramMetricsDto {

    private long totalCharts;
    private long activeCharts;
    private long closedCharts;

    private long totalProcedures;
}
