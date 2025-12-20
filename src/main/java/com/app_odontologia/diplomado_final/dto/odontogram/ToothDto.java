package com.app_odontologia.diplomado_final.dto.odontogram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToothDto {
    private Long id;
    private Integer toothNumber;
    private String toothStatus;
    private String notes;
    private Map<String, String> surfaceStates; // surface -> value
    private List<ToothAttachmentDto> attachments; // NUEVO
}
