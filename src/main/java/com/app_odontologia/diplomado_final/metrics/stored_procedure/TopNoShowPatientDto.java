package com.app_odontologia.diplomado_final.metrics.stored_procedure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopNoShowPatientDto {

    private Long patientId;
    private String fullName;
    private Integer totalNoShows;
    private Integer totalCancellations;
    private Integer consecutiveNoShows;
    private String riskLevel;
    private Instant lastNoShowAt;
}
