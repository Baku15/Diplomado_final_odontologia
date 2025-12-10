package com.app_odontologia.diplomado_final.fhir;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.springframework.stereotype.Component;
import com.app_odontologia.diplomado_final.model.entity.Patient;
import com.app_odontologia.diplomado_final.model.entity.Clinic;

import java.util.List;

@Component
public class PatientToFhirMapper {

    private final FhirContext fhirContext = FhirContext.forR4();

    public org.hl7.fhir.r4.model.Patient toFhirPatient(Patient p) {
        org.hl7.fhir.r4.model.Patient f = new org.hl7.fhir.r4.model.Patient();

        if (p.getId() != null) f.setId(p.getId().toString());
        f.setActive(Boolean.TRUE.equals(p.getActive()));

        // meta.profile (optional)
        f.getMeta().addProfile("http://example.org/fhir/StructureDefinition/patient-odontoweb");

        // identifiers: use identifiers list; fallback legacy document fields
        if (p.getIdentifiers() != null && !p.getIdentifiers().isEmpty()) {
            for (Patient.IdentifierEmbeddable id : p.getIdentifiers()) {
                Identifier ident = f.addIdentifier();
                if (id.getSystem() != null) ident.setSystem(id.getSystem());
                ident.setValue(id.getValue());
                if (id.getType() != null) {
                    // try to add as text coding
                    ident.setType(new CodeableConcept().setText(id.getType()));
                }
            }
        } else if (p.getDocumentNumber() != null) {
            f.addIdentifier()
                    .setSystem("urn:local:document")
                    .setValue(p.getDocumentNumber())
                    .setType(new CodeableConcept().setText(p.getDocumentType()));
        }

        // name: support multiple given names (split by spaces) and family
        HumanName hn = new HumanName();
        if (p.getFamilyName() != null) hn.setFamily(p.getFamilyName());
        if (p.getGivenName() != null) {
            String[] givens = p.getGivenName().trim().split("\\s+");
            for (String g : givens) if (!g.isBlank()) hn.addGiven(g);
        }
        if (hn.hasFamily() || hn.hasGiven()) f.addName(hn);

        // telecoms
        if (p.getTelecom() != null && !p.getTelecom().isEmpty()) {
            for (Patient.TelecomEmbeddable t : p.getTelecom()) {
                ContactPoint cp = new ContactPoint();
                if (t.getSystem() != null) {
                    switch (t.getSystem().toLowerCase()) {
                        case "phone":
                        case "tel":
                            cp.setSystem(ContactPoint.ContactPointSystem.PHONE);
                            break;
                        case "email":
                            cp.setSystem(ContactPoint.ContactPointSystem.EMAIL);
                            break;
                        case "fax":
                            cp.setSystem(ContactPoint.ContactPointSystem.FAX);
                            break;
                        case "url":
                            cp.setSystem(ContactPoint.ContactPointSystem.URL);
                            break;
                        default:
                            cp.setSystem(ContactPoint.ContactPointSystem.OTHER);
                    }
                }
                if (t.getValue() != null) cp.setValue(t.getValue());
                if (t.getUse() != null) {
                    switch (t.getUse().toLowerCase()) {
                        case "mobile": cp.setUse(ContactPoint.ContactPointUse.MOBILE); break;
                        case "home": cp.setUse(ContactPoint.ContactPointUse.HOME); break;
                        case "work": cp.setUse(ContactPoint.ContactPointUse.WORK); break;
                        default: /* leave unset */ break;
                    }
                }
                if (t.getRank() != null) cp.setRank(t.getRank());
                f.addTelecom(cp);
            }
        }

        // gender mapping
        if (p.getSex() != null) {
            String s = p.getSex().toLowerCase();
            switch (s) {
                case "m":
                case "male":
                    f.setGender(Enumerations.AdministrativeGender.MALE);
                    break;
                case "f":
                case "female":
                    f.setGender(Enumerations.AdministrativeGender.FEMALE);
                    break;
                case "other":
                case "o":
                    f.setGender(Enumerations.AdministrativeGender.OTHER);
                    break;
                default:
                    f.setGender(Enumerations.AdministrativeGender.UNKNOWN);
            }
        }

        // birthDate
        if (p.getBirthDate() != null) {
            f.setBirthDate(java.sql.Date.valueOf(p.getBirthDate()));
        }

        // address
        if (p.getAddressLine() != null || p.getCity() != null) {
            Address addr = new Address();
            if (p.getAddressLine() != null) addr.addLine(p.getAddressLine());
            if (p.getCity() != null) addr.setCity(p.getCity());
            if (p.getDistrict() != null) addr.setDistrict(p.getDistrict());
            if (p.getState() != null) addr.setState(p.getState());
            if (p.getPostalCode() != null) addr.setPostalCode(p.getPostalCode());
            if (p.getCountry() != null) addr.setCountry(p.getCountry());
            f.addAddress(addr);
        }

        // managingOrganization
        if (p.getClinic() != null) {
            Clinic c = p.getClinic();
            Reference orgRef = new Reference("Organization/" + c.getId());
            if (c.getNombreComercial() != null) orgRef.setDisplay(c.getNombreComercial());
            f.setManagingOrganization(orgRef);
        }

        // contacts -> Patient.contact[]
        if (p.getContacts() != null && !p.getContacts().isEmpty()) {
            for (Patient.ContactEmbeddable c : p.getContacts()) {
                org.hl7.fhir.r4.model.Patient.ContactComponent cc = new org.hl7.fhir.r4.model.Patient.ContactComponent();
                if (c.getName() != null) cc.setName(new HumanName().setText(c.getName()));
                if (c.getRelationship() != null) cc.addRelationship(new CodeableConcept().setText(c.getRelationship()));
                if (c.getTelecom() != null) {
                    // create ContactPoint, attempt to detect if it's email or phone
                    ContactPoint cp = new ContactPoint();
                    if (c.getTelecom().contains("@")) cp.setSystem(ContactPoint.ContactPointSystem.EMAIL);
                    else cp.setSystem(ContactPoint.ContactPointSystem.PHONE);
                    cp.setValue(c.getTelecom());
                    cc.addTelecom(cp);
                }
                f.addContact(cc);
            }
        }

        // extensions for reminder preferences
        if (Boolean.TRUE.equals(p.getAllowWhatsappReminders())) {
            Extension ext = new Extension("http://example.org/fhir/StructureDefinition/allow-whatsapp-reminders");
            ext.setValue(new org.hl7.fhir.r4.model.BooleanType(true));
            f.addExtension(ext);
        }
        if (Boolean.TRUE.equals(p.getAllowEmailReminders())) {
            Extension ext = new Extension("http://example.org/fhir/StructureDefinition/allow-email-reminders");
            ext.setValue(new org.hl7.fhir.r4.model.BooleanType(true));
            f.addExtension(ext);
        }

        return f;
    }

    /**
     * Produce JSON string for Patient resource.
     */
    public String toFhirJson(Patient p) {
        org.hl7.fhir.r4.model.Patient f = toFhirPatient(p);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(f);
    }

    /**
     * Produce a Bundle containing Patient and its Organization (if present).
     */
    public String toFhirBundleJson(Patient p) {
        org.hl7.fhir.r4.model.Patient f = toFhirPatient(p);
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        BundleEntryComponent be = bundle.addEntry();
        be.setFullUrl("urn:uuid:patient-" + (p.getId() != null ? p.getId() : System.nanoTime()));
        be.setResource(f);

        if (p.getClinic() != null) {
            Organization org = new Organization();
            Clinic c = p.getClinic();
            if (c.getId() != null) org.setId(c.getId().toString());
            if (c.getNombreComercial() != null) org.setName(c.getNombreComercial());
            // optionally add identifier / telecom if you later extend Clinic
            BundleEntryComponent beOrg = bundle.addEntry();
            beOrg.setFullUrl("urn:uuid:org-" + (c.getId() != null ? c.getId() : System.nanoTime()));
            beOrg.setResource(org);

            // set reference in patient to that organization fullUrl
            f.setManagingOrganization(new Reference(beOrg.getFullUrl()).setDisplay(org.getName()));
        }

        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }
}
