// src/main/java/com/app_odontologia/diplomado_final/service_impl/PatientServiceImpl.java
package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.CheckDuplicateRequest;
import com.app_odontologia.diplomado_final.dto.CheckDuplicateResponse;
import com.app_odontologia.diplomado_final.dto.PatientDeletionErrorDto;
import com.app_odontologia.diplomado_final.dto.patient.PatientCreateRequest;
import com.app_odontologia.diplomado_final.dto.patient.PatientDetailDto;
import com.app_odontologia.diplomado_final.dto.patient.PatientSummaryDto;
import com.app_odontologia.diplomado_final.dto.patient.PatientUpdateRequest;
import com.app_odontologia.diplomado_final.exception.PatientDeletionBlockedException;
import com.app_odontologia.diplomado_final.mapper.PatientMapper;
import com.app_odontologia.diplomado_final.model.entity.Clinic;
import com.app_odontologia.diplomado_final.model.entity.Patient;
import com.app_odontologia.diplomado_final.model.entity.Patient.ContactEmbeddable;
import com.app_odontologia.diplomado_final.model.entity.Patient.IdentifierEmbeddable;
import com.app_odontologia.diplomado_final.model.entity.Patient.TelecomEmbeddable;
import com.app_odontologia.diplomado_final.model.entity.Role;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.repository.*;
import com.app_odontologia.diplomado_final.service.PatientService;
import com.app_odontologia.diplomado_final.service.ActivationService;
import com.app_odontologia.diplomado_final.service.MailService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final ClinicRepository clinicRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivationService activationService;
    private final MailService mailService;
    private final ClinicalConsultationRepository clinicalConsultationRepository;


    public PatientServiceImpl(PatientRepository patientRepository,
                              ClinicRepository clinicRepository,
                              UserRepository userRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder,
                              ActivationService activationService,
                              MailService mailService,
                              ClinicalConsultationRepository clinicalConsultationRepository) {
        this.patientRepository = patientRepository;
        this.clinicRepository = clinicRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.activationService = activationService;
        this.mailService = mailService;
        this.clinicalConsultationRepository = clinicalConsultationRepository;

    }

    // delegador: mantiene compatibilidad con llamadas antiguas
    @Override
    public PatientDetailDto createPatient(Long clinicId, PatientCreateRequest request) {
        return createPatient(clinicId, request, null);
    }

    // versión principal que guarda también profileImageKey si se proporciona
    @Override
    public PatientDetailDto createPatient(Long clinicId, PatientCreateRequest request, String profileImageKey) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Clínica no encontrada"));

        // Unicidad por documento (si se envía)
        if (request.getDocumentType() != null && request.getDocumentNumber() != null) {
            boolean exists = patientRepository
                    .existsByClinicIdAndDocumentTypeAndDocumentNumberAndActiveTrue(
                            clinicId,
                            request.getDocumentType(),
                            request.getDocumentNumber()
                    );
            if (exists) {
                throw new IllegalStateException("Ya existe un paciente con ese documento en esta clínica.");
            }
        }

        // Construir telecoms (mismo helper de antes)
        List<TelecomEmbeddable> telecoms = buildTelecomsFromCreateRequestDirect(request);

        // Determinar email y phone principales
        String primaryEmail = null;
        String primaryPhone = null;
        if (telecoms != null) {
            for (TelecomEmbeddable t : telecoms) {
                if (primaryPhone == null && t.getSystem() != null && t.getSystem().equalsIgnoreCase("phone")) {
                    primaryPhone = t.getValue();
                }
                if (primaryEmail == null && t.getSystem() != null && t.getSystem().equalsIgnoreCase("email")) {
                    primaryEmail = t.getValue();
                }
                if (primaryPhone != null && primaryEmail != null) break;
            }
        }

        // Legacy override si vienen los campos "planos"
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            primaryEmail = request.getEmail().trim();
        }
        if (request.getPhoneMobile() != null && !request.getPhoneMobile().isBlank()) {
            primaryPhone = request.getPhoneMobile().trim();
        }

        // Regla: debe tener email o teléfono
        boolean hasEmail = primaryEmail != null && !primaryEmail.isBlank();
        boolean hasPhone = primaryPhone != null && !primaryPhone.isBlank();
        if (!hasEmail && !hasPhone) {
            throw new IllegalArgumentException("Debe proporcionar correo electrónico o teléfono.");
        }

        // Unicidad por email y teléfono dentro de la clínica
        if (hasEmail && patientRepository.existsByClinicIdAndEmailIgnoreCaseAndActiveTrue(clinicId, primaryEmail)) {
            throw new IllegalStateException("Ya existe un paciente con ese correo en esta clínica.");
        }
        if (hasPhone && patientRepository.existsByClinicIdAndPhoneMobileAndActiveTrue(clinicId, primaryPhone)) {
            throw new IllegalStateException("Ya existe un paciente con ese teléfono en esta clínica.");
        }

        // username: usar el enviado o generar uno
        String username;
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            username = request.getUsername().trim();
            if (patientRepository.existsByClinicIdAndUsernameAndActiveTrue(clinicId, username)) {
                throw new IllegalStateException("El nombre de usuario ya está en uso en esta clínica.");
            }
        } else {
            username = generateUsername(request.getGivenName(), request.getFamilyName());
            while (patientRepository.existsByClinicIdAndUsernameAndActiveTrue(clinicId, username)) {
                username = generateUsername(request.getGivenName(), request.getFamilyName());
            }
        }

        // Crear entidad Patient
        Patient p = new Patient();
        p.setClinic(clinic);
        p.setDocumentType(request.getDocumentType());
        p.setDocumentNumber(request.getDocumentNumber());
        p.setGivenName(request.getGivenName());
        p.setFamilyName(request.getFamilyName());
        p.setBirthDate(request.getBirthDate());
        p.setSex(request.getSex());
        p.setAddressLine(request.getAddressLine());
        p.setCity(request.getCity());
        p.setDistrict(request.getDistrict());
        p.setState(request.getState());
        p.setPostalCode(request.getPostalCode());
        p.setCountry(request.getCountry());
        p.setAllowEmailReminders(Boolean.TRUE.equals(request.getAllowEmailReminders()));
        p.setAllowWhatsappReminders(Boolean.TRUE.equals(request.getAllowWhatsappReminders()));
        p.setActive(true);

        p.setIdentifiers(buildIdentifiersFromRequest(request));
        p.setTelecom(telecoms);
        p.setContacts(buildContactsFromRequest(request));

        p.setEmail(hasEmail ? primaryEmail : null);
        p.setPhoneMobile(hasPhone ? primaryPhone : null);
        p.setUsername(username);

        if (hasEmail && hasPhone) {
            p.setContactMode(Patient.ContactMode.EMAIL_AND_PHONE);
        } else if (hasEmail) {
            p.setContactMode(Patient.ContactMode.EMAIL_ONLY);
        } else {
            p.setContactMode(Patient.ContactMode.PHONE_ONLY);
        }

        // guarda la clave de imagen (si viene)
        p.setProfileImageKey(profileImageKey);

        Patient saved = patientRepository.save(p);

        // -------------------------------
        // Crear User asociado al paciente (si corresponde)
        // -------------------------------
        if (hasEmail && userRepository != null && roleRepository != null && passwordEncoder != null) {
            // Si ya existe un usuario con ese email globalmente (tabla users), evitamos colisión.
            if (!userRepository.existsByEmailIgnoreCase(primaryEmail)) {

                // username para tabla users: aseguramos unicidad global o por clínica
                String userUsername = username;
                int attempts = 0;
                while (userRepository.existsByUsername(userUsername) && attempts < 10) {
                    userUsername = username + (int) (Math.random() * 1000);
                    attempts++;
                }
                if (userRepository.existsByUsername(userUsername)) {
                    // último recurso: username con timestamp
                    userUsername = username + System.currentTimeMillis() % 10000;
                }

                // generar contraseña temporal (si email -> no la revelamos, enviamos activación;
                // si solo phone -> la podrías enviar por SMS — aquí solo generamos)
                String tempPassword = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
                User user = new User();
                user.setUsername(userUsername);
                user.setEmail(hasEmail ? primaryEmail : null);
                user.setPassword(passwordEncoder.encode(tempPassword));
                user.setNombres(p.getGivenName());
                user.setApellidos(p.getFamilyName());
                user.setClinic(p.getClinic());
                user.setStatus(hasEmail ? com.app_odontologia.diplomado_final.model.enums.UserStatus.PENDING_ACTIVATION : com.app_odontologia.diplomado_final.model.enums.UserStatus.ACTIVE);
                user.setMustChangePassword(!hasEmail); // si tiene email, activará y elegirá contraseña; si phone-only, forzamos cambio en primer login

                // asignar rol ROLE_PATIENT
                Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                        .orElseThrow(() -> new IllegalStateException("ROLE_PATIENT no existe. Crea el rol en la tabla roles."));
                user.getRoles().add(patientRole);

                User savedUser = userRepository.save(user);

                // Nota: no hago relación bidireccional aquí (solo convenience fields)
                // Si tiene email: generar token de activación y enviar email
                if (hasEmail) {
                    try {
                        String token = activationService.createActivationToken(savedUser.getId(), 72); // 72 horas
                        String activationLink = "http://localhost:4200/activar?token=" + token;
                        mailService.sendPatientActivationEmail(primaryEmail, activationLink);
                    } catch (Exception ex) {
                        // log y no fallar creación del paciente
                        System.err.println("No se pudo generar/enviar token activación paciente: " + ex.getMessage());
                    }
                } else {
                    // phone-only: acá deberíamos enviar SMS/WhatsApp con tempPassword (no implementado)
                    System.out.println("Paciente creado sin email, phone-only. Debes enviar contraseña temporal por SMS/WhatsApp: " + tempPassword);
                }
            }
        }

        return toDetailDto(saved);
    }

    @Override
    public List<PatientSummaryDto> listPatients(Long clinicId) {
        List<Patient> patients = patientRepository
                .findByClinicIdAndActiveTrueOrderByFamilyNameAscGivenNameAsc(clinicId);
        return patients.stream().map(this::toSummaryDto).collect(Collectors.toList());
    }

    @Override
    public PatientDetailDto getPatient(Long clinicId, Long patientId) {
        Patient p = patientRepository.findByIdAndClinicIdAndActiveTrue(patientId, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
        return toDetailDto(p);
    }

    @Override
    public PatientDetailDto updatePatient(Long clinicId, Long patientId, PatientUpdateRequest request) {
        Patient p = patientRepository.findByIdAndClinicIdAndActiveTrue(patientId, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        p.setGivenName(request.getGivenName());
        p.setFamilyName(request.getFamilyName());
        p.setDocumentType(request.getDocumentType());
        p.setDocumentNumber(request.getDocumentNumber());
        p.setBirthDate(request.getBirthDate());
        p.setSex(request.getSex());
        p.setAddressLine(request.getAddressLine());
        p.setCity(request.getCity());
        p.setDistrict(request.getDistrict());
        p.setState(request.getState());
        p.setPostalCode(request.getPostalCode());
        p.setCountry(request.getCountry());
        p.setAllowEmailReminders(Boolean.TRUE.equals(request.getAllowEmailReminders()));
        p.setAllowWhatsappReminders(Boolean.TRUE.equals(request.getAllowWhatsappReminders()));

        // identifiers
        List<IdentifierEmbeddable> ids = new ArrayList<>();
        if (request.getIdentifiers() != null) {
            for (PatientCreateRequest.IdentifierDto id : request.getIdentifiers()) {
                ids.add(new IdentifierEmbeddable(trimToNull(id.getSystem()), trimToNull(id.getValue()), trimToNull(id.getType())));
            }
        }
        p.setIdentifiers(ids);

        // telecom
        List<TelecomEmbeddable> telecoms = buildTelecomsFromUpdateRequestDirect(request);
        p.setTelecom(telecoms);

        // actualizar email/phone convenience
        String primaryEmail = null;
        String primaryPhone = null;
        if (telecoms != null) {
            for (TelecomEmbeddable t : telecoms) {
                if (primaryPhone == null && "phone".equalsIgnoreCase(t.getSystem())) primaryPhone = t.getValue();
                if (primaryEmail == null && "email".equalsIgnoreCase(t.getSystem())) primaryEmail = t.getValue();
                if (primaryPhone != null && primaryEmail != null) break;
            }
        }
        p.setEmail(primaryEmail);
        p.setPhoneMobile(primaryPhone);

        // contacts
        List<ContactEmbeddable> contacts = new ArrayList<>();
        if (request.getContacts() != null) {
            for (PatientCreateRequest.ContactDto c : request.getContacts()) {
                contacts.add(new ContactEmbeddable(trimToNull(c.getName()), trimToNull(c.getRelationship()), trimToNull(c.getTelecom())));
            }
        }
        p.setContacts(contacts);

        if (request.getActive() != null) {
            p.setActive(request.getActive());
        }

        Patient saved = patientRepository.save(p);
        return toDetailDto(saved);
    }

    // --- mapeos DTOs (idéntico a la versión anterior) ---

    private PatientSummaryDto toSummaryDto(Patient p) {
        String full = (p.getGivenName() != null ? p.getGivenName() : "") + " " +
                (p.getFamilyName() != null ? p.getFamilyName() : "");
        String phone = p.getPhoneMobile();
        String email = p.getEmail();

        if ((phone == null || phone.isBlank()) || (email == null || email.isBlank())) {
            if (p.getTelecom() != null) {
                for (TelecomEmbeddable t : p.getTelecom()) {
                    if (phone == null && "phone".equalsIgnoreCase(t.getSystem())) phone = t.getValue();
                    if (email == null && "email".equalsIgnoreCase(t.getSystem())) email = t.getValue();
                    if (phone != null && email != null) break;
                }
            }
        }

        return PatientSummaryDto.builder()
                .id(p.getId())
                .givenName(p.getGivenName())
                .familyName(p.getFamilyName())
                .fullName(full.trim())
                .documentType(p.getDocumentType())
                .documentNumber(p.getDocumentNumber())
                .birthDate(p.getBirthDate())
                .phoneMobile(phone)
                .email(email)
                .build();
    }

    private PatientDetailDto toDetailDto(Patient p) {
        String full = (p.getGivenName() != null ? p.getGivenName() : "") + " " +
                (p.getFamilyName() != null ? p.getFamilyName() : "");

        List<PatientCreateRequest.IdentifierDto> idDtos = new ArrayList<>();
        if (p.getIdentifiers() != null) {
            for (IdentifierEmbeddable eb : p.getIdentifiers()) {
                PatientCreateRequest.IdentifierDto d = new PatientCreateRequest.IdentifierDto();
                d.setSystem(eb.getSystem());
                d.setValue(eb.getValue());
                d.setType(eb.getType());
                idDtos.add(d);
            }
        }

        List<PatientCreateRequest.TelecomDto> tDtos = new ArrayList<>();
        if (p.getTelecom() != null) {
            for (TelecomEmbeddable eb : p.getTelecom()) {
                PatientCreateRequest.TelecomDto td = new PatientCreateRequest.TelecomDto();
                td.setSystem(eb.getSystem());
                td.setValue(eb.getValue());
                td.setUse(eb.getUse());
                td.setRank(eb.getRank());
                tDtos.add(td);
            }
        }

        List<PatientCreateRequest.ContactDto> cDtos = new ArrayList<>();
        if (p.getContacts() != null) {
            for (ContactEmbeddable eb : p.getContacts()) {
                PatientCreateRequest.ContactDto cd = new PatientCreateRequest.ContactDto();
                cd.setName(eb.getName());
                cd.setRelationship(eb.getRelationship());
                cd.setTelecom(eb.getTelecom());
                cDtos.add(cd);
            }
        }

        return PatientDetailDto.builder()
                .id(p.getId())
                .clinicId(p.getClinic() != null ? p.getClinic().getId() : null)
                .givenName(p.getGivenName())
                .familyName(p.getFamilyName())
                .fullName(full.trim())
                .documentType(p.getDocumentType())
                .documentNumber(p.getDocumentNumber())
                .identifiers(idDtos)
                .birthDate(p.getBirthDate())
                .sex(p.getSex())
                .telecom(tDtos)
                .addressLine(p.getAddressLine())
                .city(p.getCity())
                .district(p.getDistrict())
                .state(p.getState())
                .postalCode(p.getPostalCode())
                .country(p.getCountry())
                .contacts(cDtos)
                .allowEmailReminders(Boolean.TRUE.equals(p.getAllowEmailReminders()))
                .allowWhatsappReminders(Boolean.TRUE.equals(p.getAllowWhatsappReminders()))
                .active(Boolean.TRUE.equals(p.getActive()))
                // campos nuevos:
                .username(p.getUsername())
                .email(p.getEmail())
                .phoneMobile(p.getPhoneMobile())
                .profileImageKey(p.getProfileImageKey())
                .contactMode(p.getContactMode() != null ? p.getContactMode().name() : null)
                .build();
    }

    // -----------------------
    // Helpers: build telecoms
    // (idénticos a la versión anterior)
    // -----------------------

    private List<TelecomEmbeddable> buildTelecomsFromCreateRequestDirect(PatientCreateRequest request) {
        List<TelecomEmbeddable> telecoms = new ArrayList<>();

        if (request.getTelecom() != null && !request.getTelecom().isEmpty()) {
            for (PatientCreateRequest.TelecomDto t : request.getTelecom()) {
                telecoms.add(new TelecomEmbeddable(
                        trimToNull(t.getSystem()),
                        trimToNull(t.getValue()),
                        trimToNull(t.getUse()),
                        t.getRank()
                ));
            }
        } else {
            String phoneMobile = trimToNull(getStringSafe(request.getPhoneMobile()));
            String phoneAlt = trimToNull(getStringSafe(request.getPhoneAlt()));
            String email = trimToNull(getStringSafe(request.getEmail()));
            String whatsappNumber = trimToNull(getStringSafe(request.getWhatsappNumber()));
            Boolean whatsappSameAsMobile = request.getWhatsappSameAsMobile();

            if (Boolean.TRUE.equals(whatsappSameAsMobile) &&
                    (whatsappNumber == null || whatsappNumber.isEmpty())) {
                whatsappNumber = phoneMobile;
            }

            if (phoneMobile != null)
                telecoms.add(new TelecomEmbeddable("phone", phoneMobile, "mobile", 1));
            if (phoneAlt != null)
                telecoms.add(new TelecomEmbeddable("phone", phoneAlt, "home", 2));
            if (email != null)
                telecoms.add(new TelecomEmbeddable("email", email, "home", 1));
            if (whatsappNumber != null)
                telecoms.add(new TelecomEmbeddable("phone", whatsappNumber, "mobile", 3));
        }

        return dedupeTelecomsByValue(telecoms);
    }

    private List<TelecomEmbeddable> buildTelecomsFromUpdateRequestDirect(PatientUpdateRequest request) {
        List<TelecomEmbeddable> telecoms = new ArrayList<>();

        if (request.getTelecom() != null && !request.getTelecom().isEmpty()) {
            for (PatientCreateRequest.TelecomDto t : request.getTelecom()) {
                telecoms.add(new TelecomEmbeddable(
                        trimToNull(t.getSystem()),
                        trimToNull(t.getValue()),
                        trimToNull(t.getUse()),
                        t.getRank()
                ));
            }
        } else {
            String phoneMobile = trimToNull(getStringSafe(request.getPhoneMobile()));
            String phoneAlt = trimToNull(getStringSafe(request.getPhoneAlt()));
            String email = trimToNull(getStringSafe(request.getEmail()));
            String whatsappNumber = trimToNull(getStringSafe(request.getWhatsappNumber()));
            Boolean whatsappSameAsMobile = request.getWhatsappSameAsMobile();

            if (Boolean.TRUE.equals(whatsappSameAsMobile) &&
                    (whatsappNumber == null || whatsappNumber.isEmpty())) {
                whatsappNumber = phoneMobile;
            }

            if (phoneMobile != null)
                telecoms.add(new TelecomEmbeddable("phone", phoneMobile, "mobile", 1));
            if (phoneAlt != null)
                telecoms.add(new TelecomEmbeddable("phone", phoneAlt, "home", 2));
            if (email != null)
                telecoms.add(new TelecomEmbeddable("email", email, "home", 1));
            if (whatsappNumber != null)
                telecoms.add(new TelecomEmbeddable("phone", whatsappNumber, "mobile", 3));
        }

        return dedupeTelecomsByValue(telecoms);
    }

    private List<IdentifierEmbeddable> buildIdentifiersFromRequest(PatientCreateRequest request) {
        List<IdentifierEmbeddable> ids = new ArrayList<>();
        if (request.getIdentifiers() != null) {
            for (PatientCreateRequest.IdentifierDto id : request.getIdentifiers()) {
                ids.add(new IdentifierEmbeddable(
                        trimToNull(id.getSystem()),
                        trimToNull(id.getValue()),
                        trimToNull(id.getType())
                ));
            }
        } else if (request.getDocumentNumber() != null) {
            ids.add(new IdentifierEmbeddable(
                    "urn:local:document",
                    trimToNull(request.getDocumentNumber()),
                    trimToNull(request.getDocumentType())
            ));
        }
        return ids;
    }

    private List<ContactEmbeddable> buildContactsFromRequest(PatientCreateRequest request) {
        List<ContactEmbeddable> contacts = new ArrayList<>();
        if (request.getContacts() != null) {
            for (PatientCreateRequest.ContactDto c : request.getContacts()) {
                contacts.add(new ContactEmbeddable(
                        trimToNull(c.getName()),
                        trimToNull(c.getRelationship()),
                        trimToNull(c.getTelecom())
                ));
            }
        }
        return contacts;
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String getStringSafe(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private List<TelecomEmbeddable> dedupeTelecomsByValue(List<TelecomEmbeddable> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        List<TelecomEmbeddable> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (TelecomEmbeddable t : list) {
            if (t == null || t.getValue() == null) continue;
            String v = t.getValue().trim();
            if (v.isEmpty()) continue;
            if (!seen.contains(v)) {
                seen.add(v);
                out.add(t);
            }
        }
        return out;
    }

    private String generateUsername(String given, String family) {
        String a = (given == null || given.isBlank())
                ? "user"
                : given.trim().split("\\s+")[0].toLowerCase();
        String b = (family == null || family.isBlank())
                ? "paciente"
                : family.trim().split("\\s+")[0].toLowerCase();

        String base = (a + "." + b).replaceAll("[^a-z0-9\\.]", "");
        if (base.length() < 3) base = base + "user";
        String suffix = String.format("%04d", (int) (Math.random() * 10000));
        return base + suffix;
    }

    @Override
    public CheckDuplicateResponse checkDuplicate(Long clinicId, CheckDuplicateRequest request) {
        // Prioridad: documento, luego email, luego teléfono
        if (request == null) {
            return new CheckDuplicateResponse(false, null, null, null);
        }

        // 1) Documento
        if (request.getDocumentType() != null && request.getDocumentNumber() != null) {
            boolean exists = patientRepository.existsByClinicIdAndDocumentTypeAndDocumentNumberAndActiveTrue(
                    clinicId, request.getDocumentType(), request.getDocumentNumber());
            if (exists) {
                String val = request.getDocumentType() + ":" + request.getDocumentNumber();
                return new CheckDuplicateResponse(true, "document", val,
                        "Ya existe un paciente con ese documento en esta clínica.");
            }
            return new CheckDuplicateResponse(false, null, null, null);
        }

        // 2) Email
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            boolean exists = patientRepository.existsByClinicIdAndEmailIgnoreCaseAndActiveTrue(clinicId, request.getEmail());
            if (exists) {
                return new CheckDuplicateResponse(true, "email", request.getEmail(),
                        "Ya existe un paciente con ese correo en esta clínica.");
            }
            return new CheckDuplicateResponse(false, null, null, null);
        }

        // 3) Phone
        if (request.getPhoneMobile() != null && !request.getPhoneMobile().isBlank()) {
            boolean exists = patientRepository.existsByClinicIdAndPhoneMobileAndActiveTrue(clinicId, request.getPhoneMobile());
            if (exists) {
                return new CheckDuplicateResponse(true, "phone", request.getPhoneMobile(),
                        "Ya existe un paciente con ese teléfono en esta clínica.");
            }
            return new CheckDuplicateResponse(false, null, null, null);
        }

        // nothing to check
        return new CheckDuplicateResponse(false, null, null, null);
    }

    @Override
    public Page<PatientSummaryDto> listPatients(Long clinicId, Pageable pageable) {
        Page<Patient> page = patientRepository.findByClinicId(clinicId, pageable);

        return page.map(PatientMapper::toSummary);
    }

    @Override
    public void deletePatient(Long clinicId, Long patientId) {

        Patient patient = patientRepository
                .findByIdAndClinicId(patientId, clinicId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Paciente no encontrado"));

        // 🧠 VALIDACIÓN CLÍNICA CRÍTICA
        var openConsultations =
                clinicalConsultationRepository.findOpenConsultations(patientId);

        if (!openConsultations.isEmpty()) {

            var blocking = openConsultations.stream()
                    .map(c -> new PatientDeletionErrorDto.BlockingConsultation(
                            c.getId(),
                            c.getStatus().name(),
                            c.getStartedAt() != null ? c.getStartedAt().toString() : null
                    ))
                    .toList();

            PatientDeletionErrorDto errorDto =
                    new PatientDeletionErrorDto(
                            "No se puede eliminar el paciente porque tiene consultas activas o tratamientos en curso.",
                            blocking
                    );

            throw new PatientDeletionBlockedException(errorDto);
        }


        // 🛡️ BAJA LÓGICA (soft delete)
        patient.setActive(false);
        patientRepository.save(patient);
    }


}
