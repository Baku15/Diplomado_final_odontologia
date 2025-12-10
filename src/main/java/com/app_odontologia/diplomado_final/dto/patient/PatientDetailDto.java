package com.app_odontologia.diplomado_final.dto.patient;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PatientDetailDto {

    private Long id;

    private Long clinicId;

    private String givenName;
    private String familyName;
    private String fullName;

    private String documentType;
    private String documentNumber;

    private List<PatientCreateRequest.IdentifierDto> identifiers;

    private LocalDate birthDate;
    private String sex;

    private List<PatientCreateRequest.TelecomDto> telecom;

    private String addressLine;
    private String city;
    private String district;
    private String state;
    private String postalCode;
    private String country;

    private List<PatientCreateRequest.ContactDto> contacts;

    private Boolean allowEmailReminders;
    private Boolean allowWhatsappReminders;

    private Boolean active;

    // ---- Campos añadidos (conveniencia / login / UI) ----
    private String username;
    private String email;
    private String phoneMobile;
    private String profileImageKey;
    private String contactMode; // e.g. EMAIL_ONLY | PHONE_ONLY | EMAIL_AND_PHONE
}
