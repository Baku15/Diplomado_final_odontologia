package com.app_odontologia.diplomado_final.dto.patient;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PatientUpdateRequest {

    @NotBlank
    @Size(max = 100)
    private String givenName;

    @NotBlank
    @Size(max = 100)
    private String familyName;

    private String documentType;
    private String documentNumber;

    private List<PatientCreateRequest.IdentifierDto> identifiers;

    private LocalDate birthDate;

    private String sex;

    // NEW: Legacy flat telecom fields for update compatibility
    private String phoneMobile;
    private String phoneAlt;
    @Email
    private String email;
    private String whatsappNumber;
    private Boolean whatsappSameAsMobile;

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
}
