package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.model.entity.SystemAlert;

import java.util.List;

public interface SystemAlertService {

    List<SystemAlert> getActiveAlerts(Long clinicId);

    long getActiveAlertCount(Long clinicId);

    void resolveAlert(Long alertId);
}
