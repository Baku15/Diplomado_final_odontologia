package com.app_odontologia.diplomado_final.dto.doctor;

import lombok.Data;

@Data
public class DoctorProfileUpdateDto {

    private String licenseNumber;   // matrícula
    private String specialty;       // especialidad (texto)
    private String phone;           // teléfono
    private String address;         // dirección
    private String bio;             // breve descripción / bio

    // 👇 clave: consultorio principal donde atenderá
    private Long primaryRoomId;
}
