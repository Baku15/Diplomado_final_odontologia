// src/main/java/com/app_odontologia/diplomado_final/controller/PatientValidationController.java
package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.repository.PatientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/clinic/{clinicId}/patients")
public class PatientValidationController {

    private final PatientRepository patientRepository;

    public PatientValidationController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Verifica si existe un paciente en la clínica por documento, email o teléfono.
     * Se evalúa en este orden: documentType+documentNumber, email, phoneMobile.
     *
     * Ejemplos:
     *  GET /api/clinic/1/patients/exists?documentType=CI&documentNumber=1234567
     *  GET /api/clinic/1/patients/exists?email=juan@example.com
     *  GET /api/clinic/1/patients/exists?phoneMobile=+59176543210
     */
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Object>> exists(
            @PathVariable Long clinicId,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String documentNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneMobile
    ) {
        // 1) document check (only if both provided)
        if (documentType != null && documentNumber != null) {
            boolean exists = patientRepository.existsByClinicIdAndDocumentTypeAndDocumentNumberAndActiveTrue(
                    clinicId, documentType, documentNumber);
            if (exists) {
                return ResponseEntity.ok(Map.of(
                        "exists", true,
                        "field", "document",
                        "value", documentType + ":" + documentNumber,
                        "message", "Ya existe un paciente con ese documento en esta clínica."
                ));
            } else {
                return ResponseEntity.ok(Map.of("exists", false));
            }
        }

        // 2) email check
        if (email != null && !email.isBlank()) {
            boolean exists = patientRepository.existsByClinicIdAndEmailIgnoreCaseAndActiveTrue(clinicId, email);
            if (exists) {
                return ResponseEntity.ok(Map.of(
                        "exists", true,
                        "field", "email",
                        "value", email,
                        "message", "Ya existe un paciente con ese correo en esta clínica."
                ));
            } else {
                return ResponseEntity.ok(Map.of("exists", false));
            }
        }

        // 3) phone check
        if (phoneMobile != null && !phoneMobile.isBlank()) {
            boolean exists = patientRepository.existsByClinicIdAndPhoneMobileAndActiveTrue(clinicId, phoneMobile);
            if (exists) {
                return ResponseEntity.ok(Map.of(
                        "exists", true,
                        "field", "phone",
                        "value", phoneMobile,
                        "message", "Ya existe un paciente con ese teléfono en esta clínica."
                ));
            } else {
                return ResponseEntity.ok(Map.of("exists", false));
            }
        }

        // nothing to check
        return ResponseEntity.badRequest().body(Map.of("message", "No hay parámetros de comprobación. Envía documentType+documentNumber, o email, o phoneMobile."));
    }
}
