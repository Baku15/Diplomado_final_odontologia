package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.clinical.ClinicalRecordDetailDto;
import com.app_odontologia.diplomado_final.dto.clinical.ClinicalRecordUpsertRequest;

public interface ClinicalRecordService {

    ClinicalRecordDetailDto getByPatient(Long clinicId, Long patientId);

    ClinicalRecordDetailDto createForPatient(
            Long clinicId,
            Long patientId,
            ClinicalRecordUpsertRequest request,
            String dentistUsername
    );

    ClinicalRecordDetailDto updateForPatient(
            Long clinicId,
            Long patientId,
            ClinicalRecordUpsertRequest request,
            String dentistUsername
    );

    String exportFhirJson(Long clinicId, Long patientId);

    ClinicalRecordDetailDto closeClinicalRecord(Long id);

}
