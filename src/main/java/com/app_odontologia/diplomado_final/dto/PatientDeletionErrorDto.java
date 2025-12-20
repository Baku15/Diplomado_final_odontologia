package com.app_odontologia.diplomado_final.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PatientDeletionErrorDto {

    private String message;

    // IDs de consultas que bloquean
    private List<BlockingConsultation> blockingConsultations;

    @Data
    @AllArgsConstructor
    public static class BlockingConsultation {
        private Long id;
        private String status;
        private String startedAt;
    }
}
