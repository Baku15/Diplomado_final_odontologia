package com.app_odontologia.diplomado_final.metrics.patients;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PatientsByDateDto {
    private LocalDate date;
    private long count;
}
