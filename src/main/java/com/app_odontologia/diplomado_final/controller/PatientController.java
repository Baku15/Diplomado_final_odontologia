package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.CheckDuplicateRequest;
import com.app_odontologia.diplomado_final.dto.CheckDuplicateResponse;
import com.app_odontologia.diplomado_final.dto.PatientPageResponse;
import com.app_odontologia.diplomado_final.dto.patient.PatientCreateRequest;
import com.app_odontologia.diplomado_final.dto.patient.PatientDetailDto;
import com.app_odontologia.diplomado_final.dto.patient.PatientSummaryDto;
import com.app_odontologia.diplomado_final.dto.patient.PatientUpdateRequest;
import com.app_odontologia.diplomado_final.model.entity.Patient;
import com.app_odontologia.diplomado_final.repository.PatientRepository;
import com.app_odontologia.diplomado_final.service.MinioService;
import com.app_odontologia.diplomado_final.service.PatientService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clinic/{clinicId}/patients")
public class PatientController {

    private final PatientService patientService;
    private final PatientRepository patientRepository;
    private final MinioService minioService;

    public PatientController(PatientService patientService,
                             PatientRepository patientRepository,
                             MinioService minioService) {
        this.patientService = patientService;
        this.patientRepository = patientRepository;
        this.minioService = minioService;
    }

    // --------- CRUD BÁSICO ----------

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<PatientDetailDto> createPatient(
            @PathVariable Long clinicId,
            @Valid @RequestBody PatientCreateRequest request
    ) {
        PatientDetailDto dto = patientService.createPatient(clinicId, request);
        return ResponseEntity.ok(dto);
    }

    // lista simple (si aún la usas en algún sitio)
    @GetMapping("/list-all")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<List<PatientSummaryDto>> listPatientsAll(
            @PathVariable Long clinicId
    ) {
        List<PatientSummaryDto> list = patientService.listPatients(clinicId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{patientId}")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<PatientDetailDto> getPatient(
            @PathVariable Long clinicId,
            @PathVariable Long patientId
    ) {
        PatientDetailDto dto = patientService.getPatient(clinicId, patientId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{patientId}")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<PatientDetailDto> updatePatient(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @Valid @RequestBody PatientUpdateRequest request
    ) {
        PatientDetailDto dto = patientService.updatePatient(clinicId, patientId, request);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{patientId}")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<Void> deletePatient(
            @PathVariable Long clinicId,
            @PathVariable Long patientId
    ) {
        patientService.deletePatient(clinicId, patientId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-duplicate")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<CheckDuplicateResponse> checkDuplicate(
            @PathVariable Long clinicId,
            @RequestBody CheckDuplicateRequest request) {

        CheckDuplicateResponse resp = patientService.checkDuplicate(clinicId, request);
        return ResponseEntity.ok(resp);
    }

    // --------- LISTA PAGINADA ----------

    @GetMapping
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<PatientPageResponse<PatientSummaryDto>> listPaged(
            @PathVariable Long clinicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        Sort sortObj = Sort.by(
                new Sort.Order(
                        sort.length > 1 && sort[1].equalsIgnoreCase("desc")
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC,
                        sort[0]
                )
        );

        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<PatientSummaryDto> result = patientService.listPatients(clinicId, pageable);

        PatientPageResponse<PatientSummaryDto> response = new PatientPageResponse<>(
                result.getContent(),
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );

        return ResponseEntity.ok(response);
    }

    // --------- FOTO DESDE MINIO ----------

    @GetMapping("/{patientId}/photo")
    public ResponseEntity<Resource> getPatientPhoto(
            @PathVariable Long clinicId,
            @PathVariable Long patientId
    ) {
        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado"));

            String objectKey = patient.getProfileImageKey(); // ⚠️ cambia si tu campo se llama distinto

            if (objectKey == null || objectKey.isBlank()) {
                return ResponseEntity.notFound().build();
            }

            InputStream is = minioService.getFile("patient-profiles", objectKey);
            InputStreamResource resource = new InputStreamResource(is);

            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            MediaType mediaType = objectKey.toLowerCase().endsWith(".jpg")
                    || objectKey.toLowerCase().endsWith(".jpeg")
                    ? MediaType.IMAGE_JPEG
                    : MediaType.IMAGE_PNG;

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(mediaType)
                    .body(resource);

        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Sube / actualiza la foto de un paciente existente.
     * URL: POST /api/clinic/{clinicId}/patients/{patientId}/photo
     * Body: multipart/form-data con campo "file".
     */
    @PostMapping("/{patientId}/photo")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<?> uploadPatientPhoto(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Archivo vacío"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(Map.of("message", "Tipo de archivo no permitido. Solo imágenes."));
            }

            // Buscar paciente
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado"));

            // (Opcional) podrías validar que patient.getClinic().getId().equals(clinicId)

            // Subir a MinIO
            String prefix = "clinic-" + clinicId;
            String objectKey = minioService.uploadFile("patient-profiles", prefix, file);

            // Guardar key en la entidad
            patient.setProfileImageKey(objectKey);  // ⚠️ cambia el nombre del campo si es distinto
            patientRepository.save(patient);

            // Devolvemos URL relativa para que el front pueda refrescar foto si quiere
            String photoUrl = String.format("/api/clinic/%d/patients/%d/photo", clinicId, patientId);

            return ResponseEntity.ok(Map.of(
                    "message", "Foto actualizada correctamente",
                    "photoUrl", photoUrl
            ));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Paciente no encontrado"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error subiendo foto", "detail", ex.getMessage()));
        }
    }

}