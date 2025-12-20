package com.app_odontologia.diplomado_final.dto.consultation;

import lombok.Data;

@Data
public class CloseConsultationRequest {

    private String clinicalNotes;
    private String summary;

    private Boolean requireNextAppointment;

}
