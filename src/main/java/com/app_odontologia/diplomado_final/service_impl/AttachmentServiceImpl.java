package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.attachment.AttachmentDto;
import com.app_odontologia.diplomado_final.model.entity.Attachment;
import com.app_odontologia.diplomado_final.model.enums.AttachmentType;
import com.app_odontologia.diplomado_final.repository.AttachmentRepository;
import com.app_odontologia.diplomado_final.service.AttachmentService;
import com.app_odontologia.diplomado_final.service.ClinicalConsultationService;
import com.app_odontologia.diplomado_final.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final MinioService minioService;
    private final ClinicalConsultationService clinicalConsultationService;


    @Value("${app.attachments.bucket:attachments}")
    private String attachmentsBucket;

    @Value("${app.attachments.thumbnail-size:240}")
    private int thumbnailSize;

    // =====================================================
    // MULTIPART
    // =====================================================
    @Override
    public AttachmentDto uploadAttachmentMultipart(
            Long clinicId,
            Long patientId,
            MultipartFile file,
            Long clinicalRecordId,
            Long procedureId,
            String toothReference,
            AttachmentType type,
            String notes,
            Long uploaderId
    ) throws Exception {



        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.')) : "";

        String storageKey = buildStorageKey(clinicId, patientId, ext);

        try (InputStream is = file.getInputStream()) {
            minioService.putObject(
                    is,
                    attachmentsBucket,
                    storageKey,
                    file.getContentType(),
                    file.getSize()
            );
        }

        String thumbnailKey = generateAndUploadThumbnail(storageKey);

        var consultation = clinicalConsultationService
                .getActiveConsultation(clinicId, patientId);

        Attachment attachment = Attachment.builder()
                .clinicId(clinicId)
                .patientId(patientId)
                .clinicalRecordId(clinicalRecordId)
                .procedureId(procedureId)
                .consultationId(consultation != null ? consultation.getId() : null)
                .toothReference(toothReference)
                .uploaderId(uploaderId)
                .filename(filename)
                .storageKey(storageKey)
                .thumbnailKey(thumbnailKey)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .type(type != null ? type : AttachmentType.OTHER)
                .notes(notes)
                .build();

        return toDto(attachmentRepository.save(attachment));
    }

    // =====================================================
    // PRESIGN
    // =====================================================
    @Override
    public PresignedUploadInfo generatePresignedUpload(
            Long clinicId,
            Long patientId,
            String filename,
            String contentType,
            Long sizeBytes,
            Long clinicalRecordId,
            Long procedureId,
            String toothReference,
            AttachmentType type,
            String notes,
            Long uploaderId
    ) throws Exception {

        String ext = filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.'))
                : "";

        String storageKey = buildStorageKey(clinicId, patientId, ext);

        String uploadUrl = minioService.generatePresignedPut(
                attachmentsBucket,
                storageKey,
                15 * 60
        );

        PresignedUploadInfo info = new PresignedUploadInfo();
        info.setUploadUrl(uploadUrl);
        info.setStorageKey(storageKey);
        info.setExpiresIn(900);
        info.setStorageBucket(attachmentsBucket);

        return info;
    }

    // =====================================================
    // LINK PRESIGNED
    // =====================================================
    @Override
    public AttachmentDto linkPresignedUpload(
            Long clinicId,
            Long patientId,
            String storageKey,
            String filename,
            String contentType,
            Long sizeBytes,
            Long clinicalRecordId,
            Long procedureId,
            String toothReference,
            AttachmentType type,
            String notes,
            Long uploaderId
    ) throws Exception {

        String thumbnailKey = generateAndUploadThumbnail(storageKey);
        var consultation = clinicalConsultationService
                .getActiveConsultation(clinicId, patientId);
        Attachment attachment = Attachment.builder()
                .clinicId(clinicId)
                .patientId(patientId)
                .clinicalRecordId(clinicalRecordId)
                .procedureId(procedureId)
                .toothReference(toothReference)
                .uploaderId(uploaderId)
                .consultationId(consultation != null ? consultation.getId() : null)

                .filename(filename)
                .storageKey(storageKey)
                .thumbnailKey(thumbnailKey)
                .contentType(contentType)
                .sizeBytes(sizeBytes)
                .type(type != null ? type : AttachmentType.OTHER)
                .notes(notes)
                .build();

        return toDto(attachmentRepository.save(attachment));
    }

    // =====================================================
    // QUERY
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public AttachmentDto getAttachment(Long id, Long clinicId, int ttl) throws Exception {
        Attachment a = attachmentRepository.findByIdAndClinicId(id, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));

        AttachmentDto dto = toDto(a);
        dto.setDownloadUrl(minioService.generatePresignedGet(attachmentsBucket, a.getStorageKey(), ttl));

        if (a.getThumbnailKey() != null) {
            dto.setThumbnailUrl(
                    minioService.generatePresignedGet(attachmentsBucket, a.getThumbnailKey(), ttl)
            );
        }
        return dto;
    }

    @Override
    public Page<AttachmentDto> listAttachments(Long clinicId, Long patientId, int page, int size) {
        return attachmentRepository
                .findByClinicIdAndPatientId(clinicId, patientId, PageRequest.of(page, size))
                .map(this::toDto);
    }

    @Override
    public Page<AttachmentDto> listGallery(Long clinicId, Long patientId, int page, int size) {
        return listAttachments(clinicId, patientId, page, size);
    }

    @Override
    public void deleteAttachment(Long id, Long clinicId) throws Exception {
        Attachment a = attachmentRepository.findByIdAndClinicId(id, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));

        minioService.deleteObject(attachmentsBucket, a.getStorageKey());
        if (a.getThumbnailKey() != null) {
            minioService.deleteObject(attachmentsBucket, a.getThumbnailKey());
        }
        attachmentRepository.delete(a);
    }

    // =====================================================
    // ⭐ IMÁGENES POR DIENTE (CLAVE)
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public List<AttachmentDto> listAttachmentsByTooth(
            Long clinicId,
            Long patientId,
            String toothReference,
            int ttl
    ) {
        return attachmentRepository
                .findByClinicIdAndPatientIdAndToothReference(
                        clinicId,
                        patientId,
                        toothReference,
                        Pageable.unpaged()
                )
                .getContent()
                .stream()
                .map(a -> {
                    AttachmentDto dto = toDto(a);
                    try {
                        dto.setDownloadUrl(
                                minioService.generatePresignedGet(
                                        attachmentsBucket,
                                        a.getStorageKey(),
                                        ttl
                                )
                        );
                        if (a.getThumbnailKey() != null) {
                            dto.setThumbnailUrl(
                                    minioService.generatePresignedGet(
                                            attachmentsBucket,
                                            a.getThumbnailKey(),
                                            ttl
                                    )
                            );
                        }
                    } catch (Exception ignored) {}
                    return dto;
                })
                .toList();
    }

    // =====================================================
    // HELPERS
    // =====================================================
    private String buildStorageKey(Long clinicId, Long patientId, String ext) {
        return String.format(
                "clinic-%d/patient-%d/attachments/%s%s",
                clinicId,
                patientId,
                UUID.randomUUID(),
                ext
        );
    }

    private String generateAndUploadThumbnail(String storageKey) {
        try (InputStream is = minioService.getFile(attachmentsBucket, storageKey)) {
            BufferedImage img = ImageIO.read(is);
            if (img == null) return null;

            BufferedImage thumb = new BufferedImage(
                    thumbnailSize,
                    thumbnailSize,
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D g = thumb.createGraphics();
            g.drawImage(img, 0, 0, thumbnailSize, thumbnailSize, null);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumb, "jpg", baos);

            String thumbKey = storageKey + ".thumb.jpg";

            minioService.putObject(
                    new ByteArrayInputStream(baos.toByteArray()),
                    attachmentsBucket,
                    thumbKey,
                    "image/jpeg",
                    baos.size()
            );
            return thumbKey;
        } catch (Exception ex) {
            return null;
        }
    }

    private AttachmentDto toDto(Attachment a) {
        return AttachmentDto.builder()
                .id(a.getId())
                .clinicId(a.getClinicId())
                .patientId(a.getPatientId())
                .clinicalRecordId(a.getClinicalRecordId())
                .procedureId(a.getProcedureId())

                .consultationId(a.getConsultationId())

                .toothReference(a.getToothReference())
                .uploaderId(a.getUploaderId())
                .filename(a.getFilename())
                .contentType(a.getContentType())
                .sizeBytes(a.getSizeBytes())
                .storageKey(a.getStorageKey())
                .thumbnailKey(a.getThumbnailKey())
                .type(a.getType())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
