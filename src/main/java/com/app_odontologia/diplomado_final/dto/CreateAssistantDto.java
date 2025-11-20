package com.app_odontologia.diplomado_final.dto;

import lombok.Data;

@Data
public class CreateAssistantDto {
    private String nombre;
    private String apellido;
    private String email;
    private String username; // opcional
    private String phone;
}
