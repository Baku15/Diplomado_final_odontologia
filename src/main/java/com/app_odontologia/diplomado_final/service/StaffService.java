package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.CreateAssistantDto;
import com.app_odontologia.diplomado_final.dto.CreateDoctorDto;
import com.app_odontologia.diplomado_final.model.entity.User;

public interface StaffService {
    User createDoctor(Long clinicId, CreateDoctorDto dto, String createdByUsername);
    User createAssistant(Long clinicId, CreateAssistantDto dto, String createdByUsername);
}
