package com.app_odontologia.diplomado_final.dto.doctor;

import lombok.Data;

@Data
public class CreateDoctorDto {
    private String nombre;
    private String apellido;
    private String email;
    private String username; // opcional; si no, se genera
    // campos opcionales del profile
    private String licenseNumber;
    private String specialty;
    private String phone;
    private String address;
}
