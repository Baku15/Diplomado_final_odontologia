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
public class DentalProcedureDto {
    private Long id;
    private Integer toothNumber;
    private String surface;
    private String procedureCode;
    private String type;
    private String description;
    private String performedBy;
    private Instant performedAt;
    private Instant createdAt;
    private String status;
    private Instant completedAt;

    private Integer estimatedDurationMinutes;
    private Long estimatedCostCents;

    // si existe attachment, sólo devolvemos su id (FE pedirá URL si la necesita)
    private Long attachmentId;

    private Long createdInConsultationId;
    private Long completedInConsultationId;
}
