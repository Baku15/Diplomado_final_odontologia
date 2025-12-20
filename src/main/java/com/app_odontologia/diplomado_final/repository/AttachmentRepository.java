package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.dto.attachment.AttachmentDto;
import com.app_odontologia.diplomado_final.model.entity.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    Page<Attachment> findByClinicIdAndPatientId(Long clinicId, Long patientId, Pageable pageable);

    Page<Attachment> findByClinicId(Long clinicId, Pageable pageable);

    List<Attachment> findByClinicIdAndProcedureId(Long clinicId, Long procedureId);

    Optional<Attachment> findByIdAndClinicId(Long id, Long clinicId);

    Page<Attachment> findByClinicIdAndPatientIdAndToothReference(
            Long clinicId,
            Long patientId,
            String toothReference,
            Pageable pageable
    );



}
