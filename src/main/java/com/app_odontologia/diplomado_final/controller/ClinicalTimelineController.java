// controller/ClinicalTimelineController.java
package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.timeline.ClinicalTimelineItemDto;
import com.app_odontologia.diplomado_final.service.ClinicalTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/clinic/{clinicId}/patients/{patientId}/timeline")
@RequiredArgsConstructor
public class ClinicalTimelineController {

    private final ClinicalTimelineService timelineService;


    @GetMapping
    public ResponseEntity<Page<ClinicalTimelineItemDto>> getTimeline(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        return ResponseEntity.ok(
                timelineService.getTimeline(
                        clinicId, patientId, page, size, status, from, to
                )
        );
    }

}
