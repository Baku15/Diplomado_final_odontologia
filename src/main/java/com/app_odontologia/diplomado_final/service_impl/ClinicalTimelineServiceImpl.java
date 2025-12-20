// service_impl/ClinicalTimelineServiceImpl.java
package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.timeline.ClinicalTimelineItemDto;
import com.app_odontologia.diplomado_final.mapper.ClinicalConsultationMapper;
import com.app_odontologia.diplomado_final.model.entity.ClinicalConsultation;
import com.app_odontologia.diplomado_final.repository.ClinicalConsultationRepository;
import com.app_odontologia.diplomado_final.service.ClinicalConsultationService;
import com.app_odontologia.diplomado_final.service.ClinicalTimelineService;
import com.app_odontologia.diplomado_final.service.DentalChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClinicalTimelineServiceImpl implements ClinicalTimelineService {

    private final ClinicalConsultationService consultationService;
    private final DentalChartService dentalChartService;
    private final ClinicalConsultationRepository consultationRepository;




    @Override
    public List<ClinicalTimelineItemDto> getTimeline(Long clinicId, Long patientId) {

        return consultationService
                .listConsultations(clinicId, patientId)
                .stream()
                .map(c -> ClinicalTimelineItemDto.builder()
                        .consultationId(c.getId())
                        .status(c.getStatus())
                        .startedAt(c.getStartedAt())
                        .endedAt(c.getEndedAt())
                        .clinicalNotes(c.getClinicalNotes())
                        .summary(c.getSummary())
                        .procedures(
                                dentalChartService.listProceduresByConsultation(c.getId())
                        )
                        .build()
                )
                .toList();
    }

    @Override
    public Page<ClinicalTimelineItemDto> getTimeline(
            Long clinicId,
            Long patientId,
            int page,
            int size,
            String status,
            Instant from,
            Instant to
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ClinicalConsultation> consultations;

        if (from != null && to != null) {
            consultations =
                    consultationRepository.findByClinicIdAndPatientIdAndStartedAtBetween(
                            clinicId, patientId, from, to, pageable
                    );
        } else if (status != null) {
            consultations =
                    consultationRepository.findByClinicIdAndPatientIdAndStatus(
                            clinicId,
                            patientId,
                            ClinicalConsultation.ConsultationStatus.valueOf(status),
                            pageable
                    );
        } else {
            consultations =
                    consultationRepository.findByClinicIdAndPatientId(
                            clinicId, patientId, pageable
                    );
        }

        return consultations.map(c ->
                ClinicalTimelineItemDto.builder()
                        .consultationId(c.getId())
                        .status(c.getStatus().name())
                        .startedAt(c.getStartedAt())
                        .endedAt(c.getEndedAt())
                        .clinicalNotes(c.getClinicalNotes())
                        .summary(c.getSummary())
                        .procedures(
                                dentalChartService.listProceduresByConsultation(c.getId())
                        )
                        .build()
        );
    }
}
