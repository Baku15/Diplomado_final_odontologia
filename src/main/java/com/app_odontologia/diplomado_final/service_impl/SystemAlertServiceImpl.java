package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.SystemAlertDto;
import com.app_odontologia.diplomado_final.model.entity.SystemAlert;
import com.app_odontologia.diplomado_final.repository.SystemAlertRepository;
import com.app_odontologia.diplomado_final.service.SystemAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemAlertServiceImpl implements SystemAlertService {

    private final SystemAlertRepository alertRepository;

    @Override
    public List<SystemAlertDto> getActiveAlerts(Long clinicId) {
        return alertRepository
                .findByClinicIdAndResolvedFalseOrderByCreatedAtDesc(clinicId)
                .stream()
                .map(SystemAlertDto::fromEntity)
                .toList();
    }


    @Override
    public long getActiveAlertCount(Long clinicId) {
        return alertRepository.countByClinicIdAndResolvedFalse(clinicId);
    }

    @Override
    @Transactional
    public void resolveAlert(Long alertId) {
        SystemAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alerta no encontrada"));

        alert.setResolved(true);
    }
}
