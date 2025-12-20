// service/ClinicalTimelineService.java
package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.timeline.ClinicalTimelineItemDto;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

public interface ClinicalTimelineService {

    List<ClinicalTimelineItemDto> getTimeline(Long clinicId, Long patientId);

    Page<ClinicalTimelineItemDto> getTimeline(
            Long clinicId,
            Long patientId,
            int page,
            int size,
            String status,
            Instant from,
            Instant to
    );
}
