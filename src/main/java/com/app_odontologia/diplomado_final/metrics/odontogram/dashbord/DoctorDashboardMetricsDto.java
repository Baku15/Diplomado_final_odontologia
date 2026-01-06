package com.app_odontologia.diplomado_final.metrics.odontogram.dashbord;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DoctorDashboardMetricsDto {

    // 1️⃣ Consultas con actividad clínica real
    private long totalConsultationsWithActivity;

    // 2️⃣ Total de dientes intervenidos
    private long totalTeethIntervened;

    // 3️⃣ Total de procedimientos
    private long totalProcedures;

    // 4️⃣ Procedimientos completados
    private long completedProcedures;

    // 5️⃣ Procedimientos pendientes
    private long pendingProcedures;

    // 6️⃣ Dientes con alta carga clínica
    private long teethWithHighClinicalLoad;

    // 7️⃣ Dientes con evidencia fotográfica
    private long teethWithImages;
}
