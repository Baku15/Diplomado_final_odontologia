// src/main/java/com/app_odontologia/diplomado_final/controller/PatientControllerMultipart.java
package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.patient.PatientCreateRequest;
import com.app_odontologia.diplomado_final.dto.patient.PatientDetailDto;
import com.app_odontologia.diplomado_final.exception.StorageUnavailableException;
import com.app_odontologia.diplomado_final.service.MinioService;
import com.app_odontologia.diplomado_final.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/clinic/{clinicId}/patients")
@RequiredArgsConstructor
public class PatientControllerMultipart {

    private final PatientService patientService;
    private final MinioService minioService;
    private final ObjectMapper objectMapper;

    /**
     * Multipart endpoint: expects multipart/form-data with:
     *  - payload: JSON string (PatientCreateRequest)  (RequestPart)
     *  - photo: optional image file                    (RequestPart)
     *
     * Note: the pure JSON endpoint (application/json) should be handled by the existing
     * PatientController (the one that accepts @RequestBody). This method only accepts multipart.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPatient(
            @PathVariable Long clinicId,
            @RequestPart(value = "payload", required = false) String payloadJson,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        try {
            // payload is required (as JSON string) when using multipart
            if (payloadJson == null || payloadJson.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "payload requerido"));
            }

            // deserialize JSON payload
            PatientCreateRequest req = objectMapper.readValue(payloadJson, PatientCreateRequest.class);

            // handle optional photo
            String profileKey = null;
            if (photo != null && !photo.isEmpty()) {
                String contentType = photo.getContentType();
                if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                    // return 415 Unsupported Media Type when the uploaded file is not an image
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .body(Map.of("message", "Tipo de archivo no permitido. Solo imágenes."));
                }
                String prefix = "clinic-" + clinicId;
                profileKey = minioService.uploadFile("patient-profiles", prefix, photo);
            }

            PatientDetailDto created = patientService.createPatient(clinicId, req, profileKey);
            return ResponseEntity.ok(created);
        } catch (StorageUnavailableException sue) {
            sue.printStackTrace();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Servicio de almacenamiento no disponible", "detail", sue.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error creando paciente", "detail", ex.getMessage()));
        }
    }
}
