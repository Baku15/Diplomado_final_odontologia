package com.app_odontologia.diplomado_final.dto.clinical;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DentalHistoryDto {

    private LocalDate lastDentalVisitDate;
    private String visitReasonType;

    private String previousTreatments;

    private Boolean hasBruxism;
    private Boolean hasOnychophagia;
    private String otherHabits;

    private Integer brushingFrequencyPerDay;
    private Boolean usesFloss;
    private Boolean usesMouthwash;

    private String dentalFearLevel;

    private String orthodonticHistory;

    // 👉 aquí guardaremos el texto del textarea "Historia odontológica"
    private String notes;

    // ====== Soporte para JSON como String ======

    @JsonCreator
    public DentalHistoryDto(String notes) {
        this.notes = notes;
    }

    @JsonValue
    public String asJson() {
        return this.notes;
    }
}
