package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.ClinicRoomDto;
import com.app_odontologia.diplomado_final.dto.ClinicRoomRequestDto;
import com.app_odontologia.diplomado_final.model.entity.Clinic;
import com.app_odontologia.diplomado_final.model.entity.ClinicRoom;
import com.app_odontologia.diplomado_final.repository.ClinicRepository;
import com.app_odontologia.diplomado_final.repository.ClinicRoomRepository;
import com.app_odontologia.diplomado_final.service.ClinicRoomService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClinicRoomServiceImpl implements ClinicRoomService {

    private final ClinicRepository clinicRepo;
    private final ClinicRoomRepository roomRepo;

    @Override
    public List<ClinicRoomDto> listActiveByClinic(Long clinicId) {
        Clinic clinic = clinicRepo.findById(clinicId)
                .orElseThrow(() -> new IllegalStateException("Clinic no encontrada: " + clinicId));

        return roomRepo.findByClinicAndActiveTrue(clinic)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ClinicRoomDto create(Long clinicId, ClinicRoomRequestDto dto) {
        Clinic clinic = clinicRepo.findById(clinicId)
                .orElseThrow(() -> new IllegalStateException("Clinic no encontrada: " + clinicId));

        ClinicRoom room = new ClinicRoom();
        room.setClinic(clinic);
        room.setName(dto.getName().trim());
        room.setCode(dto.getCode() != null ? dto.getCode().trim() : null);
        room.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        room.setActive(true);

        ClinicRoom saved = roomRepo.save(room);
        return toDto(saved);
    }

    @Override
    public ClinicRoomDto update(Long clinicId, Long roomId, ClinicRoomRequestDto dto) {
        ClinicRoom room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Consultorio no encontrado: " + roomId));

        // Seguridad extra: verificar que el room pertenezca a esa clínica
        if (!room.getClinic().getId().equals(clinicId)) {
            throw new IllegalStateException("El consultorio no pertenece a la clínica indicada.");
        }

        room.setName(dto.getName().trim());
        room.setCode(dto.getCode() != null ? dto.getCode().trim() : null);
        room.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);

        ClinicRoom saved = roomRepo.save(room);
        return toDto(saved);
    }

    @Override
    public void deactivate(Long clinicId, Long roomId) {
        ClinicRoom room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Consultorio no encontrado: " + roomId));

        if (!room.getClinic().getId().equals(clinicId)) {
            throw new IllegalStateException("El consultorio no pertenece a la clínica indicada.");
        }

        room.setActive(false);
        roomRepo.save(room);
    }

    // ------------------------
    // Mapeo entidad -> DTO
    // ------------------------
    private ClinicRoomDto toDto(ClinicRoom room) {
        ClinicRoomDto dto = new ClinicRoomDto();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setCode(room.getCode());
        dto.setDescription(room.getDescription());
        dto.setActive(room.getActive());
        return dto;
    }
}
