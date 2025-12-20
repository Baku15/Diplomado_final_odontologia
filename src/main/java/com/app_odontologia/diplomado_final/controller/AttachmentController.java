package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.attachment.*;
import com.app_odontologia.diplomado_final.model.enums.AttachmentType;
import com.app_odontologia.diplomado_final.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/clinic/{clinicId}/patients/{patientId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    /* ============================================================
     * 1️⃣ UPLOAD MULTIPART DIRECTO (fallback / legacy)
     * ============================================================ */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadMultipart(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "clinicalRecordId", required = false) Long clinicalRecordId,
            @RequestParam(value = "procedureId", required = false) Long procedureId,
            @RequestParam(value = "toothReference", required = false) String toothReference,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId
    ) {
        try {
            AttachmentType attachmentType = AttachmentType.OTHER;
            if (type != null && !type.isBlank()) {
                try {
                    attachmentType = AttachmentType.valueOf(type);
                } catch (IllegalArgumentException ignored) {
                }
            }

            AttachmentDto dto = attachmentService.uploadAttachmentMultipart(
                    clinicId,
                    patientId,
                    file,
                    clinicalRecordId,
                    procedureId,
                    toothReference,
                    attachmentType,
                    notes,
                    uploaderId
            );

            return ResponseEntity.ok(dto);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("message", "Error subiendo attachment", "detail", ex.getMessage())
            );
        }
    }

    /* ============================================================
     * 2️⃣ PRESIGN (DTO → DTO)  ✅ CLAVE
     * ============================================================ */
    @PostMapping("/presign")
    public ResponseEntity<PresignedUploadResponse> presignUpload(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @RequestBody PresignedUploadRequest req
    ) {
        try {
            var info = attachmentService.generatePresignedUpload(
                    clinicId,
                    patientId,
                    req.getFilename(),
                    req.getContentType(),
                    null,                // size (no requerido aquí)
                    null,                // clinicalRecordId (opcional)
                    null,                // procedureId
                    req.getToothReference(),
                    AttachmentType.PHOTO,
                    null,
                    null
            );

            return ResponseEntity.ok(
                    PresignedUploadResponse.builder()
                            .uploadUrl(info.getUploadUrl())
                            .storageKey(info.getStorageKey())
                            .expiresIn(info.getExpiresIn())
                            .build()
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /* ============================================================
     * 3️⃣ LINK PRESIGNED UPLOAD (DTO → DTO)  ✅ CLAVE
     * ============================================================ */
    @PostMapping("/link")
    public ResponseEntity<AttachmentDto> linkAttachment(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @RequestBody AttachmentLinkRequest req
    ) {
        try {
            if (req.getStorageKey() == null || req.getStorageKey().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(null);
            }

            AttachmentType type = AttachmentType.OTHER;
            if (req.getType() != null) {
                try {
                    type = AttachmentType.valueOf(req.getType());
                } catch (IllegalArgumentException ignored) {
                }
            }

            AttachmentDto dto = attachmentService.linkPresignedUpload(
                    clinicId,
                    patientId,
                    req.getStorageKey(),      // 🔥 NUNCA NULL
                    req.getFilename(),
                    req.getContentType(),
                    req.getSizeBytes(),
                    req.getClinicalRecordId(),
                    null,                     // procedureId
                    req.getToothReference(),
                    type,
                    req.getNotes(),
                    null
            );

            return ResponseEntity.ok(dto);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /* ============================================================
     * 4️⃣ LISTADO GENERAL
     * ============================================================ */
    @GetMapping
    public ResponseEntity<Page<AttachmentDto>> list(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                attachmentService.listAttachments(clinicId, patientId, page, size)
        );
    }

    /* ============================================================
     * 5️⃣ GET INDIVIDUAL
     * ============================================================ */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Long id,
            @RequestParam(defaultValue = "300") int urlTtlSeconds
    ) {
        try {
            return ResponseEntity.ok(
                    attachmentService.getAttachment(id, clinicId, urlTtlSeconds)
            );
        } catch (Exception ex) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Attachment no encontrado"));
        }
    }

    /* ============================================================
     * 6️⃣ DELETE
     * ============================================================ */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Long id
    ) {
        try {
            attachmentService.deleteAttachment(id, clinicId);
            return ResponseEntity.ok(Map.of("message", "deleted"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Error borrando attachment"));
        }
    }

    /* ============================================================
     * 7️⃣ GALERÍA
     * ============================================================ */
    @GetMapping("/gallery")
    public ResponseEntity<Page<AttachmentDto>> listGallery(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(
                attachmentService.listGallery(clinicId, patientId, page, size)
        );
    }
}
