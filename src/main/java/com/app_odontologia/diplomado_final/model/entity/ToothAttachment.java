package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "dental_tooth_attachments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tooth_attachment_tooth_attachment", columnNames = {"tooth_id", "attachment_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToothAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación al diente
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tooth_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tooth_attachment_tooth"))
    private Tooth tooth;

    // Relación al attachment ya existente
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attachment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tooth_attachment_attachment"))
    private Attachment attachment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
