package com.app_odontologia.diplomado_final.metrics.stored_procedure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyAppointmentSummaryDto {

    private Integer year;
    private Integer month;
    private Long totalAppointments;
    private Long completed;
    private Long cancelled;
    private Long noShow;
    private Double noShowRate;
}
