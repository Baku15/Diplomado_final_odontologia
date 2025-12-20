package com.app_odontologia.diplomado_final.metrics.stored_procedure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClinicKpiDto {

    private Long totalAppointments;
    private Long completed;
    private Long noShow;
    private Long lateCancellations;
    private Double noShowRate;
    private Long totalMinutesLost;
    private Long blockedPatients;
}
