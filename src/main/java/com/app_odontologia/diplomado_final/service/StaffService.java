package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.*;
import com.app_odontologia.diplomado_final.dto.doctor.*;
import com.app_odontologia.diplomado_final.model.entity.User;

public interface StaffService {
    User createDoctor(Long clinicId, CreateDoctorDto dto, String createdByUsername);
    User createAssistant(Long clinicId, CreateAssistantDto dto, String createdByUsername);

    DoctorInvitationDto inviteDoctor(Long clinicId,
                                     InviteDoctorRequestDto request,
                                     String requestedBy);

    DoctorInvitationDto getInvitationByToken(String token);
    DoctorInvitationStatusDto getDoctorInvitationStatus(String token);

    void registerDoctorFromInvitation(String token,
                                      DoctorInvitationRegisterRequestDto request);


}
