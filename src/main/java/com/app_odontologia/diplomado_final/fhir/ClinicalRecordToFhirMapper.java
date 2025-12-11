package com.app_odontologia.diplomado_final.fhir;

import ca.uhn.fhir.context.FhirContext;
import com.app_odontologia.diplomado_final.model.entity.Clinic;
import com.app_odontologia.diplomado_final.model.entity.ClinicalRecord;
import com.app_odontologia.diplomado_final.model.entity.Patient;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class ClinicalRecordToFhirMapper {

    private final FhirContext fhirContext = FhirContext.forR4();

    private final PatientToFhirMapper patientToFhirMapper;

    /**
     * Construye un Bundle FHIR (COLLECTION) que contiene:
     * - Patient
     * - Organization (clínica)
     * - Composition (resumen de historia clínica)
     * - Observations para signos vitales (si están presentes)
     */
    public String toFhirBundleJson(ClinicalRecord cr) {
        if (cr == null) {
            throw new IllegalArgumentException("ClinicalRecord no puede ser null");
        }

        Patient patientEntity = cr.getPatient();
        Clinic clinicEntity = cr.getClinic();

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        // ===== Patient =====
        org.hl7.fhir.r4.model.Patient fhirPatient =
                patientToFhirMapper.toFhirPatient(patientEntity);

        BundleEntryComponent patientEntry = bundle.addEntry();
        String patientFullUrl = "urn:uuid:patient-" +
                (patientEntity.getId() != null ? patientEntity.getId() : System.nanoTime());
        patientEntry.setFullUrl(patientFullUrl);
        patientEntry.setResource(fhirPatient);

        // ===== Organization (clinic) =====
        Organization org = new Organization();
        if (clinicEntity != null) {
            if (clinicEntity.getId() != null) {
                org.setId(clinicEntity.getId().toString());
            }
            if (clinicEntity.getNombreComercial() != null) {
                org.setName(clinicEntity.getNombreComercial());
            }
        }

        BundleEntryComponent orgEntry = bundle.addEntry();
        String orgFullUrl = "urn:uuid:org-" +
                (clinicEntity != null && clinicEntity.getId() != null ? clinicEntity.getId() : System.nanoTime());
        orgEntry.setFullUrl(orgFullUrl);
        orgEntry.setResource(org);

        // actualizar managingOrganization para usar el fullUrl del bundle
        fhirPatient.setManagingOrganization(new Reference(orgFullUrl).setDisplay(org.getName()));

        // ===== Composition: documento de historia clínica =====
        Composition composition = buildComposition(cr, patientFullUrl);

        BundleEntryComponent compEntry = bundle.addEntry();
        String compFullUrl = "urn:uuid:composition-" +
                (cr.getId() != null ? cr.getId() : System.nanoTime());
        compEntry.setFullUrl(compFullUrl);
        compEntry.setResource(composition);

        // ===== Observations de signos vitales (si hay) =====
        if (cr.getVitalSigns() != null) {
            ClinicalRecord.VitalSignsEmbeddable v = cr.getVitalSigns();

            // Presión arterial
            if (v.getBloodPressureSystolic() != null || v.getBloodPressureDiastolic() != null) {
                Observation bp = buildBloodPressureObservation(cr, v, patientFullUrl);
                BundleEntryComponent bpEntry = bundle.addEntry();
                bpEntry.setFullUrl("urn:uuid:obs-bp-" + (cr.getId() != null ? cr.getId() : System.nanoTime()));
                bpEntry.setResource(bp);
                composition.addSection()
                        .setTitle("Presión arterial")
                        .addEntry(new Reference(bpEntry.getFullUrl()));
            }

            // Frecuencia cardíaca
            if (v.getHeartRate() != null) {
                Observation hr = buildSimpleVitalObservation(
                        "Frecuencia cardíaca",
                        "bpm",
                        v.getHeartRate().doubleValue(),
                        patientFullUrl
                );
                BundleEntryComponent hrEntry = bundle.addEntry();
                hrEntry.setFullUrl("urn:uuid:obs-hr-" + (cr.getId() != null ? cr.getId() : System.nanoTime()));
                hrEntry.setResource(hr);
                composition.addSection()
                        .setTitle("Frecuencia cardíaca")
                        .addEntry(new Reference(hrEntry.getFullUrl()));
            }

            // Temperatura
            if (v.getTemperatureCelsius() != null) {
                Observation temp = buildSimpleVitalObservation(
                        "Temperatura corporal",
                        "°C",
                        v.getTemperatureCelsius(),
                        patientFullUrl
                );
                BundleEntryComponent tEntry = bundle.addEntry();
                tEntry.setFullUrl("urn:uuid:obs-temp-" + (cr.getId() != null ? cr.getId() : System.nanoTime()));
                tEntry.setResource(temp);
                composition.addSection()
                        .setTitle("Temperatura corporal")
                        .addEntry(new Reference(tEntry.getFullUrl()));
            }

            // Saturación O2
            if (v.getOxygenSaturation() != null) {
                Observation spo2 = buildSimpleVitalObservation(
                        "Saturación de oxígeno",
                        "%",
                        v.getOxygenSaturation().doubleValue(),
                        patientFullUrl
                );
                BundleEntryComponent sEntry = bundle.addEntry();
                sEntry.setFullUrl("urn:uuid:obs-spo2-" + (cr.getId() != null ? cr.getId() : System.nanoTime()));
                sEntry.setResource(spo2);
                composition.addSection()
                        .setTitle("Saturación de oxígeno")
                        .addEntry(new Reference(sEntry.getFullUrl()));
            }
        }

        // Serializar a JSON
        return fhirContext.newJsonParser()
                .setPrettyPrint(true)
                .encodeResourceToString(bundle);
    }

    private Composition buildComposition(ClinicalRecord cr, String patientFullUrl) {
        Composition comp = new Composition();

        if (cr.getId() != null) {
            comp.setId(cr.getId().toString());
        }

        comp.setStatus(Composition.CompositionStatus.FINAL);
        comp.setType(new CodeableConcept().setText("Historia clínica odontológica"));

        comp.setSubject(new Reference(patientFullUrl));

        if (cr.getCreatedAt() != null) {
            comp.setDate(Date.from(cr.getCreatedAt()));
        } else {
            comp.setDate(new Date());
        }

        Patient p = cr.getPatient();
        String title = "Historia clínica odontológica";
        if (p != null) {
            String fullName = (p.getGivenName() != null ? p.getGivenName() : "") +
                    " " +
                    (p.getFamilyName() != null ? p.getFamilyName() : "");
            title = "Historia clínica odontológica de " + fullName.trim();
        }
        comp.setTitle(title);

        // meta.profile (opcional)
        comp.getMeta().addProfile("http://example.org/fhir/StructureDefinition/clinical-record-odontoweb");

        // ===== Secciones de texto narrativo =====

        // Motivo de consulta y enfermedad actual
        if (cr.getChiefComplaint() != null || cr.getCurrentIllness() != null) {
            Composition.SectionComponent sec = comp.addSection();
            sec.setTitle("Motivo de consulta y enfermedad actual");

            StringBuilder sb = new StringBuilder();
            if (cr.getChiefComplaint() != null) {
                sb.append("Motivo de consulta: ").append(cr.getChiefComplaint()).append("\n");
            }
            if (cr.getCurrentIllness() != null) {
                sb.append("Enfermedad actual: ").append(cr.getCurrentIllness());
            }

            if (!sb.isEmpty()) {
                Narrative text = new Narrative();
                text.setStatus(Narrative.NarrativeStatus.GENERATED);
                text.setDivAsString("<div><pre>" + escapeHtml(sb.toString()) + "</pre></div>");
                sec.setText(text);
            }
        }

        // Antecedentes médicos
        if (cr.getMedicalHistory() != null) {
            Composition.SectionComponent sec = comp.addSection();
            sec.setTitle("Antecedentes médicos");
        }

        // Historia odontológica
        if (cr.getDentalHistory() != null) {
            Composition.SectionComponent sec = comp.addSection();
            sec.setTitle("Historia odontológica");
        }

        // Examen extraoral / intraoral
        if (cr.getExtraoralExam() != null || cr.getIntraoralExam() != null) {
            Composition.SectionComponent sec = comp.addSection();
            sec.setTitle("Examen clínico");
        }

        // Diagnóstico y plan
        if (cr.getInitialDiagnosticSummary() != null ||
                cr.getInitialTreatmentPlanSummary() != null ||
                cr.getInitialPrognosis() != null) {

            Composition.SectionComponent sec = comp.addSection();
            sec.setTitle("Diagnóstico inicial y plan de tratamiento");

            StringBuilder sb = new StringBuilder();
            if (cr.getInitialDiagnosticSummary() != null) {
                sb.append("Diagnóstico inicial: ").append(cr.getInitialDiagnosticSummary()).append("\n");
            }
            if (cr.getInitialTreatmentPlanSummary() != null) {
                sb.append("Plan de tratamiento inicial: ")
                        .append(cr.getInitialTreatmentPlanSummary())
                        .append("\n");
            }
            if (cr.getInitialPrognosis() != null) {
                sb.append("Pronóstico inicial: ").append(cr.getInitialPrognosis());
            }

            if (!sb.isEmpty()) {
                Narrative text = new Narrative();
                text.setStatus(Narrative.NarrativeStatus.GENERATED);
                text.setDivAsString("<div><pre>" + escapeHtml(sb.toString()) + "</pre></div>");
                sec.setText(text);
            }
        }

        return comp;
    }

    private Observation buildBloodPressureObservation(ClinicalRecord cr,
                                                      ClinicalRecord.VitalSignsEmbeddable v,
                                                      String patientFullUrl) {

        Observation obs = new Observation();
        obs.setStatus(Observation.ObservationStatus.FINAL);
        obs.setCode(new CodeableConcept().setText("Presión arterial"));

        obs.setSubject(new Reference(patientFullUrl));

        if (cr.getCreatedAt() != null) {
            obs.setEffective(new DateTimeType(Date.from(cr.getCreatedAt())));
        }

        if (v.getBloodPressureSystolic() != null ||
                v.getBloodPressureDiastolic() != null) {

            Observation.ObservationComponentComponent systolic = new Observation.ObservationComponentComponent();
            systolic.setCode(new CodeableConcept().setText("Sistólica"));
            if (v.getBloodPressureSystolic() != null) {
                systolic.setValue(
                        new Quantity()
                                .setValue(v.getBloodPressureSystolic())
                                .setUnit("mmHg")
                );
            }

            Observation.ObservationComponentComponent diastolic = new Observation.ObservationComponentComponent();
            diastolic.setCode(new CodeableConcept().setText("Diastólica"));
            if (v.getBloodPressureDiastolic() != null) {
                diastolic.setValue(
                        new Quantity()
                                .setValue(v.getBloodPressureDiastolic())
                                .setUnit("mmHg")
                );
            }

            obs.addComponent(systolic);
            obs.addComponent(diastolic);
        }

        return obs;
    }

    private Observation buildSimpleVitalObservation(String label,
                                                    String unit,
                                                    Double value,
                                                    String patientFullUrl) {
        Observation obs = new Observation();
        obs.setStatus(Observation.ObservationStatus.FINAL);
        obs.setCode(new CodeableConcept().setText(label));
        obs.setSubject(new Reference(patientFullUrl));

        if (value != null) {
            obs.setValue(
                    new Quantity()
                            .setValue(value)
                            .setUnit(unit)
            );
        }

        return obs;
    }

    private String escapeHtml(String in) {
        if (in == null) return "";
        return in
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
