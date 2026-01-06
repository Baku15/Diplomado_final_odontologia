package com.app_odontologia.diplomado_final.metrics.odontogram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToothHealthSummaryDto {

    private Long healthyTeeth;     // sin procedimientos
    private Long affectedTeeth;    // con ≥1 procedimiento
    private Long totalTeeth;
}
