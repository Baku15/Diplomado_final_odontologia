package com.app_odontologia.diplomado_final.exception;

import com.app_odontologia.diplomado_final.dto.PatientDeletionErrorDto;
import lombok.Getter;

@Getter
public class PatientDeletionBlockedException extends RuntimeException {

    private final PatientDeletionErrorDto payload;

    public PatientDeletionBlockedException(PatientDeletionErrorDto payload) {
        super(payload.getMessage());
        this.payload = payload;
    }
}
