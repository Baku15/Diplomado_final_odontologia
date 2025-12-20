package com.app_odontologia.diplomado_final.metrics.odontogram;

import com.app_odontologia.diplomado_final.model.entity.DentalChart.ChartStatus;
import com.app_odontologia.diplomado_final.repository.DentalChartRepository;
import com.app_odontologia.diplomado_final.repository.DentalProcedureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OdontogramMetricsServiceImpl
        implements OdontogramMetricsService {

    private final DentalChartRepository chartRepository;
    private final DentalProcedureRepository procedureRepository;

    @Override
    public OdontogramMetricsDto getMetrics(Long clinicId) {

        long totalCharts =
                chartRepository.countByClinicId(clinicId);

        long activeCharts =
                chartRepository.countByClinicIdAndStatus(
                        clinicId,
                        ChartStatus.ACTIVE
                );

        long closedCharts =
                chartRepository.countByClinicIdAndStatus(
                        clinicId,
                        ChartStatus.CLOSED
                );

        long totalProcedures =
                procedureRepository.countByChartClinicId(clinicId);

        return new OdontogramMetricsDto(
                totalCharts,
                activeCharts,
                closedCharts,
                totalProcedures
        );
    }
}
