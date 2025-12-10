// src/main/java/com/app_odontologia/diplomado_final/dto/patient/PatientCreateRequest.java
package com.app_odontologia.diplomado_final.dto.patient;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PatientCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String givenName;

    @NotBlank
    @Size(max = 100)
    private String familyName;

    // Legacy primary identifier
    private String documentType;
    private String documentNumber;

    // Support for multiple identifiers (system/value/type)
    private List<IdentifierDto> identifiers;

    private LocalDate birthDate;

    private String sex; // M/F/O/...

    // NEW: Legacy flat telecom fields (kept for backward compatibility)
    private String phoneMobile;
    private String phoneAlt;

    @Email
    private String email;

    private String whatsappNumber;
    private Boolean whatsappSameAsMobile;

    // Telecom: support multiple entries (preferred new format)
    private List<TelecomDto> telecom;

    // Address fields
    private String addressLine;
    private String city;
    private String district;
    private String state;
    private String postalCode;
    private String country;

    // Contacts (next of kin)
    private List<ContactDto> contacts;

    // Reminder preferences
    private Boolean allowEmailReminders;
    private Boolean allowWhatsappReminders;

    // Optional client-provided username (we will generate one if absent)
    @Size(min = 3, max = 80)
    private String username;

    // DTO helper inner classes
    @Data
    public static class IdentifierDto {
        private String system;
        private String value;
        private String type;
    }

    @Data
    public static class TelecomDto {
        private String system; // phone, email, url
        private String value;
        private String use; // mobile, home, work
        private Integer rank;
    }

    @Data
    public static class ContactDto {
        private String name;
        private String relationship;
        private String telecom; // single string for simplicity
    }
}
