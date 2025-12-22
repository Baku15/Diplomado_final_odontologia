package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.SystemAlertDto;
import com.app_odontologia.diplomado_final.model.entity.SystemAlert;

import java.util.List;

public interface SystemAlertService {

    List<SystemAlertDto> getActiveAlerts(Long clinicId);

    long getActiveAlertCount(Long clinicId);

    void resolveAlert(Long alertId);
}
