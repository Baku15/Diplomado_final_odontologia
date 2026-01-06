package com.app_odontologia.diplomado_final.metrics.odontogram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureTimelineItemDto {

    private LocalDate date;
    private Long count;
}
