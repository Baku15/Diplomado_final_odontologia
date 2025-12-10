package com.app_odontologia.diplomado_final.dto.patient;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class PatientSummaryDto {

    private Long id;
    private String givenName;
    private String familyName;
    private String fullName;
    private String documentType;
    private String documentNumber;
    private LocalDate birthDate;
    private String phoneMobile; // convenience field (first mobile found)
    private String email;
    Instant createdAt;
// convenience field (first email found)
}
