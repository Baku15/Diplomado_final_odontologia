// src/main/java/com/app_odontologia/diplomado_final/model/entity/Patient.java
package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "patients",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_patient_clinic_document",
                        columnNames = {"clinic_id", "document_type", "document_number"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // relación con clínica (managingOrganization)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_patient_clinic"))
    private Clinic clinic;

    // --- Primary identifier (legacy fields kept for compatibility) ---
    @Column(name = "document_type", length = 20)
    private String documentType;

    @Column(name = "document_number", length = 50)
    private String documentNumber;

    // --- Support for multiple identifiers (FHIR identifier[]) ---
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "patient_identifiers", joinColumns = @JoinColumn(name = "patient_id"))
    private List<IdentifierEmbeddable> identifiers = new ArrayList<>();

    // --- Nombre ---
    @Column(name = "given_name", length = 100, nullable = false)
    private String givenName;

    @Column(name = "family_name", length = 100, nullable = false)
    private String familyName;

    @Column(name = "full_name_norm", length = 220)
    private String fullNameNorm;

    // --- Demográficos ---
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "sex", length = 10)
    private String sex;


    // --- Telecom (phone/email) — soporte múltiple (FHIR telecom[]) ---
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "patient_telecom", joinColumns = @JoinColumn(name = "patient_id"))
    private List<TelecomEmbeddable> telecom = new ArrayList<>();

    // --- Legacy flat convenience columns (agregados) ---
    @Column(name = "email", length = 160)
    private String email; // convenience: principal email (may duplicate telecom)

    @Column(name = "phone_mobile", length = 60)
    private String phoneMobile; // convenience: principal mobile

    // NEW: profile image key (MinIO object key) — opcional
    @Column(name = "profile_image_key", length = 512)
    private String profileImageKey;

    // NEW: contact mode (email/phone/both)
    @Enumerated(EnumType.STRING)
    @Column(name = "contact_mode", length = 30)
    private ContactMode contactMode = ContactMode.UNSPECIFIED;

    // --- Dirección más granular ---
    @Column(name = "address_line", length = 255)
    private String addressLine;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", length = 30)
    private String postalCode;

    @Column(name = "country", length = 2)
    private String country;

    // --- Contactos (nextOfKin / emergency contact) ---
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "patient_contacts", joinColumns = @JoinColumn(name = "patient_id"))
    private List<ContactEmbeddable> contacts = new ArrayList<>();

    // --- Preferencias de recordatorio (extensions en FHIR) ---
    @Column(name = "allow_email_reminders")
    @ColumnDefault("false")
    private Boolean allowEmailReminders;

    @Column(name = "allow_whatsapp_reminders")
    @ColumnDefault("false")
    private Boolean allowWhatsappReminders;

    // --- Estado lógico ---
    @Column(name = "active")
    @ColumnDefault("true")
    private Boolean active;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    // NEW: optional username (for future login if we create user)
    @Column(name = "username", length = 80)
    private String username;

    @PrePersist
    @PreUpdate
    public void prePersistUpdate() {
        if (this.givenName != null && this.familyName != null) {
            String full = (this.givenName + " " + this.familyName).trim();
            this.fullNameNorm = normalizeText(full);
        }
        if (this.active == null) {
            this.active = true;
        }
        // populate convenience email/phoneMobile from telecom if empty
        if ((this.email == null || this.email.isBlank()) && this.telecom != null) {
            for (TelecomEmbeddable t : this.telecom) {
                if ("email".equalsIgnoreCase(t.getSystem())) {
                    this.email = t.getValue();
                    break;
                }
            }
        }
        if ((this.phoneMobile == null || this.phoneMobile.isBlank()) && this.telecom != null) {
            for (TelecomEmbeddable t : this.telecom) {
                if ("phone".equalsIgnoreCase(t.getSystem())) {
                    this.phoneMobile = t.getValue();
                    break;
                }
            }
        }
    }

    private String normalizeText(String input) {
        if (input == null) return null;
        return input.toUpperCase();
    }

    // NEW: contact mode enum
    public enum ContactMode {
        EMAIL_ONLY,
        PHONE_ONLY,
        EMAIL_AND_PHONE,
        UNSPECIFIED
    }

    // --- Embeddables ---
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdentifierEmbeddable {
        @Column(name = "id_system", length = 150)
        private String system;   // e.g. urn:bolivia:ci or http://hospital.org/ids

        @Column(name = "id_value", length = 150)
        private String value;

        @Column(name = "id_type", length = 80)
        private String type; // free text description
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TelecomEmbeddable {
        @Column(name = "telecom_system", length = 20)
        private String system; // phone, email, fax, url

        @Column(name = "telecom_value", length = 160)
        private String value;

        @Column(name = "telecom_use", length = 20)
        private String use; // home, work, mobile

        @Column(name = "telecom_rank")
        private Integer rank;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactEmbeddable {
        @Column(name = "contact_name", length = 200)
        private String name;

        @Column(name = "contact_relationship", length = 80)
        private String relationship;

        @Column(name = "contact_telecom", length = 160)
        private String telecom; // single value for simplicity (phone/email)
    }
}
