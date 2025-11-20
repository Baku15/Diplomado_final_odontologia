package com.app_odontologia.diplomado_final.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClinicRoomRequestDto {

    @NotBlank
    @Size(max = 80)
    private String name;

    @Size(max = 40)
    private String code;

    @Size(max = 255)
    private String description;
}
