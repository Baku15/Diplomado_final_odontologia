package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.attachment.AttachmentDto;
import com.app_odontologia.diplomado_final.dto.attachment.AttachmentLinkRequest;
import com.app_odontologia.diplomado_final.dto.attachment.PresignedUploadRequest;
import com.app_odontologia.diplomado_final.dto.attachment.PresignedUploadResponse;
import com.app_odontologia.diplomado_final.dto.clinical.ClinicalRecordDetailDto;
import com.app_odontologia.diplomado_final.dto.clinical.ClinicalRecordUpsertRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    PresignedUploadResponse generatePresignedUploadUrl(Long clinicId, Long patientId, PresignedUploadRequest req, String username) throws Exception;
    AttachmentDto linkAttachment(Long clinicId, Long patientId, AttachmentLinkRequest req, String username);
    Page<AttachmentDto> listAttachments(Long clinicId, Long patientId, Pageable pageable);
    void deleteAttachment(Long clinicId, Long patientId, Long attachmentId, String username) throws Exception;


}
