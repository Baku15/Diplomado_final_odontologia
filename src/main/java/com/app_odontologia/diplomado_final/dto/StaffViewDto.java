package com.app_odontologia.diplomado_final.dto;

import lombok.Data;

import java.util.Set;

@Data
public class StaffViewDto {
    private Long id;
    private String username;
    private String nombre;
    private String apellido;
    private String email;
    private Set<String> roles;
    private String status;
    private String phone;
}
