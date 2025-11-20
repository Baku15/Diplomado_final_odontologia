package com.app_odontologia.diplomado_final.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserMeDto {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private Long clinicId;
    private Boolean mustCompleteProfile = false;
}
