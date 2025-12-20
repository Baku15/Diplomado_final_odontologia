package com.app_odontologia.diplomado_final.dto.odontogram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DentalChartDto {

    private Long id;
    private Long clinicId;
    private Long patientId;
    private Long clinicalRecordId; // opcional
    private Integer version;
    private String status;

    private List<ToothDto> teeth;
    private List<DentalProcedureDto> procedures;

    private Instant createdAt;
    private Instant updatedAt;
}
