// src/main/java/com/app_odontologia/diplomado_final/service/PatientService.java
package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.CheckDuplicateRequest;
import com.app_odontologia.diplomado_final.dto.CheckDuplicateResponse;
import com.app_odontologia.diplomado_final.dto.patient.PatientCreateRequest;
import com.app_odontologia.diplomado_final.dto.patient.PatientDetailDto;
import com.app_odontologia.diplomado_final.dto.patient.PatientSummaryDto;
import com.app_odontologia.diplomado_final.dto.patient.PatientUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PatientService {

    // versión clásica (delegará a la versión con profileImageKey = null)
    PatientDetailDto createPatient(Long clinicId, PatientCreateRequest request);

    // nueva sobrecarga que acepta la clave devuelta por MinIO (profile image)
    PatientDetailDto createPatient(Long clinicId, PatientCreateRequest request, String profileImageKey);

    List<PatientSummaryDto> listPatients(Long clinicId);

    PatientDetailDto getPatient(Long clinicId, Long patientId);

    PatientDetailDto updatePatient(Long clinicId, Long patientId, PatientUpdateRequest request);

    CheckDuplicateResponse checkDuplicate(Long clinicId, CheckDuplicateRequest request);

    Page<PatientSummaryDto> listPatients(Long clinicId, Pageable pageable);

    void deletePatient(Long clinicId, Long patientId);


}
