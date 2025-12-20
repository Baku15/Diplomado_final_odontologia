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
public class IntraoralExamDto {

    private String oralMucosa;
    private String gingivalStatus;
    private String plaqueLevel;
    private String calculusLevel;
    private String tongueFindings;
    private String palateFindings;
    private String floorOfMouthFindings;
    private String occlusionNotes;

    private String periodontalStatus;
    private String cariesRisk;

    // 👉 Usaremos este campo para el textarea "Examen intraoral"
    private String otherFindings;

    // ====== Soporte para JSON como String ======


}
