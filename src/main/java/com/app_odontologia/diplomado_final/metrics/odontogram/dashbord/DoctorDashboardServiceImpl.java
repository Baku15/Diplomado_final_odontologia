package com.app_odontologia.diplomado_final.metrics.odontogram.dashbord;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorDashboardServiceImpl implements DoctorDashboardService {

    private final DoctorDashboardRepository repository;

    // 🔧 Regla clínica: alta carga ≥ 3 procedimientos
    private static final long HIGH_LOAD_THRESHOLD = 3;

    @Override
    public DoctorDashboardMetricsDto getDoctorMetrics(
            Long clinicId,
            Long dentistId,
            Instant start,
            Instant end
    ) {

        return DoctorDashboardMetricsDto.builder()
                .totalConsultationsWithActivity(
                        repository.countConsultationsWithActivity(dentistId, start, end)
                )
                .totalProcedures(
                        repository.countTotalProcedures(clinicId, start, end)
                )
                .completedProcedures(
                        repository.countCompletedProcedures(clinicId, start, end)
                )
                .pendingProcedures(
                        repository.countPendingProcedures(clinicId, start, end)
                )
                .totalTeethIntervened(
                        repository.countTeethIntervened(clinicId, start, end)
                )
                .teethWithHighClinicalLoad(
                        repository.countTeethWithHighLoad(
                                clinicId,
                                start,
                                end,
                                HIGH_LOAD_THRESHOLD
                        )
                )
                .teethWithImages(
                        repository.countTeethWithImages(clinicId, start, end)
                )
                .build();
    }

    @Override
    public List<ToothInterventionDetailDto> getTeethInterventionDetail(
            Long clinicId,
            Instant start,
            Instant end
    ) {
        return repository.findTeethInterventionDetail(clinicId, start, end)
                .stream()
                .map(row -> {

                    Number tooth = (Number) row[0];
                    Number count = (Number) row[1];

                    return new ToothInterventionDetailDto(
                            tooth != null ? tooth.intValue() : null,
                            count != null ? count.longValue() : 0L
                    );
                })
                .toList();
    }


}
