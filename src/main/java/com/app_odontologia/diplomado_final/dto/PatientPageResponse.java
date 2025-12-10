package com.app_odontologia.diplomado_final.dto;

import java.util.List;

public record PatientPageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}
