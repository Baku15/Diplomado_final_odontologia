// dto/DoctorProfileUpdateDto.java
package com.app_odontologia.diplomado_final.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorProfileUpdateDto {

    @NotBlank
    private String licenseNumber;

    private String specialty;
    private String phone;
    private String address;
    private String bio;

    // 🔹 id del consultorio seleccionado en el combo
    private Long roomId;
}
