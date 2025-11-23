package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.DoctorProfileMeDto;
import com.app_odontologia.diplomado_final.model.entity.ClinicRoom;
import com.app_odontologia.diplomado_final.model.entity.DoctorProfile;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.repository.ClinicRoomRepository;
import com.app_odontologia.diplomado_final.repository.DoctorProfileRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.service.DoctorProfileSelfService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorProfileSelfServiceImpl implements DoctorProfileSelfService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final ClinicRoomRepository clinicRoomRepository;

    private User getUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
    }

    @Override
    @Transactional
    public DoctorProfileMeDto getMyProfile(String username) {
        User user = getUserOrThrow(username);

        DoctorProfile profile = doctorProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException(
                        "Todavía no has completado tu perfil profesional de odontólogo."
                ));

        Long primaryRoomId = profile.getPrimaryRoom() != null
                ? profile.getPrimaryRoom().getId()
                : null;

        return new DoctorProfileMeDto(
                profile.getLicenseNumber(),
                profile.getSpecialty(),
                profile.getPhone(),
                profile.getAddress(),
                profile.getBio(),
                primaryRoomId
        );
    }

    @Override
    @Transactional
    public DoctorProfileMeDto updateMyProfile(String username, DoctorProfileMeDto dto) {
        User user = getUserOrThrow(username);

        DoctorProfile profile = doctorProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró perfil profesional. Completa primero el wizard inicial."
                ));

        if (user.getClinic() == null || user.getClinic().getId() == null) {
            throw new IllegalStateException("Tu usuario no está asociado a ninguna clínica.");
        }
        Long clinicId = user.getClinic().getId();

        // Validar que el consultorio pertenece a la misma clínica
        ClinicRoom room = clinicRoomRepository.findById(dto.primaryRoomId())
                .filter(r -> r.getClinic() != null &&
                        clinicId.equals(r.getClinic().getId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "El consultorio seleccionado no pertenece a tu clínica."
                ));

        // Campos editables
        profile.setLicenseNumber(dto.licenseNumber());
        profile.setSpecialty(dto.specialty());
        profile.setPhone(dto.phone());
        profile.setAddress(dto.address());
        profile.setBio(dto.bio());
        profile.setPrimaryRoom(room);

        doctorProfileRepository.save(profile);

        return dto;
    }
}
