package com.app_odontologia.diplomado_final.model.entity;

import com.app_odontologia.diplomado_final.model.enums.AttachmentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "attachments", indexes = {
        @Index(name = "idx_attachments_clinic_patient", columnList = "clinic_id, patient_id"),
        @Index(name = "idx_attachments_procedure", columnList = "procedure_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "clinical_record_id")
    private Long clinicalRecordId;

    @Column(name = "procedure_id")
    private Long procedureId;

    @Column(name = "tooth_reference", length = 32)
    private String toothReference;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @Column(name = "filename", nullable = false, length = 300)
    private String filename;

    @Column(name = "storage_key", nullable = false, length = 1000)
    private String storageKey;

    @Column(name = "thumbnail_key", length = 1000)
    private String thumbnailKey;

    @Column(name = "content_type", length = 200)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 40)
    private AttachmentType type;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "consultation_id")
    private Long consultationId;
}
