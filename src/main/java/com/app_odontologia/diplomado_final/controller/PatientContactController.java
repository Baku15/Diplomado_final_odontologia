package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.model.entity.Patient;
import com.app_odontologia.diplomado_final.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientContactController {

    private final PatientRepository patientRepository;

    /**
     * Endpoint ligero para obtener datos de contacto del paciente.
     * Usado por agenda / creación de citas.
     */
    @GetMapping("/{patientId}/contact")
    public ResponseEntity<Map<String, Object>> getPatientContact(
            @PathVariable Long patientId
    ) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        String email = patient.getEmail();

        return ResponseEntity.ok(
                Map.of(
                        "email", email,
                        "canSendEmail", email != null && !email.isBlank()
                )
        );
    }
}
