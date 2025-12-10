package com.app_odontologia.diplomado_final.dto.doctor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DoctorProfileMeDto(
        @NotBlank String licenseNumber,
        String specialty,
        String phone,
        String address,
        String bio,
        @NotNull Long primaryRoomId
) {}
