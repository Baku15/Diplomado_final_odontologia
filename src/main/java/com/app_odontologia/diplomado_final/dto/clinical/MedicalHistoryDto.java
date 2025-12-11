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
public class MedicalHistoryDto {

    private Boolean hasDiabetes;
    private Boolean hasHypertension;
    private Boolean hasCardioDisease;
    private Boolean hasRespiratoryDisease;
    private Boolean hasCoagulationDisorder;
    private Boolean hasKidneyDisease;
    private Boolean hasLiverDisease;
    private Boolean hasNeurologicDisease;
    private Boolean hasThyroidDisease;
    private Boolean hasImmunosuppression;

    private Boolean hasDrugAllergy;
    private String drugAllergyDetails;

    private Boolean hasAnestheticAllergy;
    private String anestheticAllergyDetails;

    private String otherAllergies;

    private String previousSurgeries;
    private String currentMedications;

    private String smokingStatus;
    private String alcoholUse;
    private String drugUse;

    private Boolean isPregnant;
    private Integer pregnancyWeeks;

    // 👉 aquí guardaremos el texto del textarea "Antecedentes médicos"
    private String notes;

    // ====== Soporte para JSON como String ======

    /**
     * Permite deserializar JSON tipo:
     *   "medicalHistory": "texto libre..."
     */
    @JsonCreator
    public MedicalHistoryDto(String notes) {
        this.notes = notes;
    }

    /**
     * Hace que al serializar se envíe solo el texto:
     *   "medicalHistory": "texto libre..."
     */
    @JsonValue
    public String asJson() {
        return this.notes;
    }
}
