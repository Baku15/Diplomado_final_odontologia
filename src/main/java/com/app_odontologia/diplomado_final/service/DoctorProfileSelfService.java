package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.DoctorProfileMeDto;

public interface DoctorProfileSelfService {

    DoctorProfileMeDto getMyProfile(String username);

    DoctorProfileMeDto updateMyProfile(String username, DoctorProfileMeDto dto);
}
