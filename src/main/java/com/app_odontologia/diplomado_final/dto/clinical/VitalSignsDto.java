package com.app_odontologia.diplomado_final.dto.clinical;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalSignsDto {

    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private Integer heartRate;
    private Integer respiratoryRate;
    private Double temperatureCelsius;
    private Integer oxygenSaturation;

    private String notes;
}
