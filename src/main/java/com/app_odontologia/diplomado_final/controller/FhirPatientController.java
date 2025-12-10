package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.fhir.PatientToFhirMapper;
import com.app_odontologia.diplomado_final.model.entity.Patient;
import com.app_odontologia.diplomado_final.repository.PatientRepository;
import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/fhir/patient")
public class FhirPatientController {

    private final PatientRepository patientRepository;
    private final PatientToFhirMapper mapper;
    private final FhirContext fhirContext = FhirContext.forR4();

    public FhirPatientController(PatientRepository patientRepository, PatientToFhirMapper mapper) {
        this.patientRepository = patientRepository;
        this.mapper = mapper;
    }

    /**
     * GET /fhir/patient/{id}
     * Query params:
     *  - _bundle=true  -> returns a Bundle with Patient (+Organization)
     *  - _format=json  -> accepted but no special handling beyond content-type
     */
    @PreAuthorize("@aclService.canAccessPatient(#id, authentication)")
    @GetMapping(value = "/{id}", produces = "application/fhir+json")
    public ResponseEntity<String> getFhirPatient(
            @PathVariable Long id,
            @RequestParam(name = "_bundle", required = false, defaultValue = "false") boolean bundle,
            @RequestParam(name = "_format", required = false) String format
    ) {
        Optional<Patient> maybe = patientRepository.findById(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/fhir+json; charset=utf-8");

        if (maybe.isEmpty()) {
            OperationOutcome oc = new OperationOutcome();
            OperationOutcomeIssueComponent issue = oc.addIssue();
            issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
            issue.setCode(OperationOutcome.IssueType.NOTFOUND);
            issue.setDiagnostics("Patient with id " + id + " not found");
            String out = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(oc);
            return new ResponseEntity<>(out, headers, HttpStatus.NOT_FOUND);
        }

        if (bundle) {
            String bundleJson = mapper.toFhirBundleJson(maybe.get());
            return new ResponseEntity<>(bundleJson, headers, HttpStatus.OK);
        } else {
            String patientJson = mapper.toFhirJson(maybe.get());
            return new ResponseEntity<>(patientJson, headers, HttpStatus.OK);
        }
    }
}