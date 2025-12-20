package com.app_odontologia.diplomado_final.fhir;

import com.app_odontologia.diplomado_final.model.entity.DentalChart;
import com.app_odontologia.diplomado_final.model.entity.Tooth;
import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Mapper base: convierte un DentalChart en un Bundle FHIR minimalista.
 * Usa HAPI FHIR (FhirContext) para serializar a JSON.
 */
@Component
public class DentalChartToFhirMapper {

    // Reutiliza un FhirContext por clase (es thread-safe para este uso).
    private final FhirContext fhirContext = FhirContext.forR4();

    public String toFhirBundleJson(DentalChart chart) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        // Composition (opcional) con metadatos sencillos
        Composition comp = new Composition();
        comp.setTitle("Odontograma paciente " + (chart.getPatient() != null ? chart.getPatient().getId() : "unknown"));
        comp.setDate(new Date());
        bundle.addEntry().setResource(comp);

        // Observations por diente/superficie (simple)
        if (chart.getTeeth() != null) {
            for (Tooth t : chart.getTeeth()) {
                if (t.getSurfaceStates() == null) continue;
                for (String surface : t.getSurfaceStates().keySet()) {
                    Observation o = new Observation();
                    o.setStatus(Observation.ObservationStatus.FINAL);
                    o.getCode().addCoding()
                            .setSystem("http://snomed.info/sct")
                            .setCode("160715003")
                            .setDisplay("Dental finding");
                    o.setSubject(new Reference("Patient/" + (chart.getPatient() != null ? chart.getPatient().getId() : "unknown")));
                    o.setIssued(new Date());

                    // extensiones sencillas para número de diente y superficie
                    o.addExtension()
                            .setUrl("http://example.org/fhir/StructureDefinition/tooth-number")
                            .setValue(new StringType(String.valueOf(t.getToothNumber())));
                    o.addExtension()
                            .setUrl("http://example.org/fhir/StructureDefinition/surface")
                            .setValue(new StringType(surface));

                    // valor: el texto que describa el estado en esa superficie
                    o.setValue(new StringType(t.getSurfaceStates().get(surface)));

                    bundle.addEntry().setResource(o);
                }
            }
        }

        // Serializar bundle a JSON usando HAPI
        try {
            return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
        } catch (Exception ex) {
            // En caso de error, devolvemos un JSON mínimo para no romper flujo
            return "{\"error\":\"no se pudo serializar el bundle FHIR\"}";
        }
    }
}
