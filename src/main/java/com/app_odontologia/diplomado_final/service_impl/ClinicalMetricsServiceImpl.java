package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.metrics.ClinicalConsultationMetricsDto;
import com.app_odontologia.diplomado_final.model.entity.ClinicalConsultation;
import com.app_odontologia.diplomado_final.repository.ClinicalConsultationRepository;
import com.app_odontologia.diplomado_final.service.ClinicalMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClinicalMetricsServiceImpl implements ClinicalMetricsService {

    private final ClinicalConsultationRepository consultationRepository;

    @Override
    public ClinicalConsultationMetricsDto getConsultationMetrics(Long clinicId) {

        long total = consultationRepository.countByClinicId(clinicId);

        long active = consultationRepository.countByClinicIdAndStatus(
                clinicId,
                ClinicalConsultation.ConsultationStatus.ACTIVE
        );

        long closed = consultationRepository.countByClinicIdAndStatus(
                clinicId,
                ClinicalConsultation.ConsultationStatus.CLOSED
        );

        Double avgDuration =
                consultationRepository.findAverageDurationMinutes(clinicId);

        return new ClinicalConsultationMetricsDto(
                total,
                active,
                closed,
                avgDuration != null ? avgDuration : 0
        );
    }
}
