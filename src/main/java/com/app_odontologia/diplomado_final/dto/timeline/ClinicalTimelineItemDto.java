// dto/timeline/ClinicalTimelineItemDto.java
package com.app_odontologia.diplomado_final.dto.timeline;

import com.app_odontologia.diplomado_final.dto.odontogram.DentalProcedureDto;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ClinicalTimelineItemDto {

    private Long consultationId;
    private String status;
    private Instant startedAt;
    private Instant endedAt;

    private String clinicalNotes;
    private String summary;

    private List<DentalProcedureDto> procedures;
}
