package com.app_odontologia.diplomado_final.dto.consultation;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ClinicalConsultationDto {

    private Long id;

    private Long clinicId;
    private Long patientId;
    private Long dentistId;
    private String dentistName;

    private Long dentalChartId;

    private String status;

    private Instant startedAt;
    private Instant endedAt;

    private String clinicalNotes;
    private String summary;
    private boolean requiresFollowUp;

}
