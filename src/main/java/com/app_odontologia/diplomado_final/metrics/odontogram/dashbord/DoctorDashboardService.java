package com.app_odontologia.diplomado_final.metrics.odontogram.dashbord;


import java.time.Instant;
import java.util.List;

public interface DoctorDashboardService {

    DoctorDashboardMetricsDto getDoctorMetrics(
            Long clinicId,
            Long dentistId,
            Instant start,
            Instant end
    );

    List<ToothInterventionDetailDto> getTeethInterventionDetail(
            Long clinicId,
            Instant start,
            Instant end
    );

}
