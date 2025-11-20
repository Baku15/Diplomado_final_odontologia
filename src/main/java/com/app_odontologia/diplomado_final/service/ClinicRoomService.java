package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.ClinicRoomDto;
import com.app_odontologia.diplomado_final.dto.ClinicRoomRequestDto;

import java.util.List;

public interface ClinicRoomService {

    List<ClinicRoomDto> listActiveByClinic(Long clinicId);

    ClinicRoomDto create(Long clinicId, ClinicRoomRequestDto dto);

    ClinicRoomDto update(Long clinicId, Long roomId, ClinicRoomRequestDto dto);

    void deactivate(Long clinicId, Long roomId);
}
