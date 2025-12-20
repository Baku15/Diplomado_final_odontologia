package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.AppointmentAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentAuditRepository
        extends JpaRepository<AppointmentAudit, Long> {
}
