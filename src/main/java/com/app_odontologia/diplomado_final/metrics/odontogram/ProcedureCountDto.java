package com.app_odontologia.diplomado_final.metrics.odontogram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureCountDto {

    private String type;   // DentalProcedure.type
    private Long count;
}
