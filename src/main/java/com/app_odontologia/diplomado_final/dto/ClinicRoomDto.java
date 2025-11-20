package com.app_odontologia.diplomado_final.dto;

import lombok.Data;

@Data
public class ClinicRoomDto {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Boolean active;
}
