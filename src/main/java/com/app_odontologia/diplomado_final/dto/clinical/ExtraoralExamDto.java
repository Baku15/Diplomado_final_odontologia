package com.app_odontologia.diplomado_final.dto.clinical;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtraoralExamDto {

    private String faceSymmetry;
    private String tmjFindings;
    private String lymphNodes;

    // 👉 Usaremos este campo para el textarea "Examen extraoral"
    private String otherFindings;

    // ====== Soporte para JSON como String ======

    @JsonCreator
    public ExtraoralExamDto(String otherFindings) {
        this.otherFindings = otherFindings;
    }

    @JsonValue
    public String asJson() {
        return this.otherFindings;
    }
}
