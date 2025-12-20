package com.app_odontologia.diplomado_final.dto.odontogram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProcedureRequest {
    private Integer toothNumber;
    private String surface;
    private String type;
    private String description;
    private String performedBy;
    private Instant performedAt;

    private String procedureCode;
    private Integer estimatedDurationMinutes;
    private Long estimatedCostCents;

    private Long attachmentId;
}
