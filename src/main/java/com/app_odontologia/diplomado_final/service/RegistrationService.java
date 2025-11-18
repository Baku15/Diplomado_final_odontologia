package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.ApproveRegistrationDto;
import com.app_odontologia.diplomado_final.dto.RegistrationRequestCreateDto;
import com.app_odontologia.diplomado_final.dto.RegistrationRequestViewDto;
import com.app_odontologia.diplomado_final.model.entity.RegistrationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegistrationService {

    RegistrationRequest create(RegistrationRequestCreateDto dto);

    Page<RegistrationRequestViewDto> listPending(Pageable pageable);

    void approve(Long id, ApproveRegistrationDto dto, String adminUsername);

    void reject(Long id, String adminUsername, String reason);
}