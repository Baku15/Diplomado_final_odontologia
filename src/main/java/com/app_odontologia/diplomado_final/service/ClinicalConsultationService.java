package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.consultation.ClinicalConsultationDto;
import com.app_odontologia.diplomado_final.dto.consultation.CloseConsultationRequest;
import com.app_odontologia.diplomado_final.dto.odontogram.DentalProcedureDto;

import java.util.List;

public interface ClinicalConsultationService {

    ClinicalConsultationDto getActiveConsultation(Long clinicId, Long patientId);


    ClinicalConsultationDto closeConsultation(
            Long consultationId,
            CloseConsultationRequest request,
            String dentistUsername
    );

    List<DentalProcedureDto> listProceduresByConsultation(Long consultationId);


    List<ClinicalConsultationDto> listConsultations(Long clinicId, Long patientId);

    ClinicalConsultationDto getById(Long clinicId, Long patientId, Long id);

    ClinicalConsultationDto getActiveOrInProgress(Long clinicId, Long patientId);

    ClinicalConsultationDto enterOdontogram(
            Long clinicId,
            Long patientId,
            String dentistUsername
    );

    void leaveOdontogram(
            Long consultationId,
            boolean hasClinicalChanges
    );


}
