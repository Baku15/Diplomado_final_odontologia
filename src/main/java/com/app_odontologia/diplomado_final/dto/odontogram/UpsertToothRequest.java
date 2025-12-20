package com.app_odontologia.diplomado_final.dto.odontogram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertToothRequest {
    private Integer toothNumber;
    private String toothStatus;
    private String notes;
    private Map<String, String> surfaceStates;

    // NUEVO: ids de attachments que queremos añadir al diente
    private List<Long> attachmentIdsToAdd;

    // NUEVO: ids de attachments que queremos quitar del diente
    private List<Long> attachmentIdsToRemove;
}
