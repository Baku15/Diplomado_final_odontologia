package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.odontogram.metrics.OdontogramMetricsDto;
import com.app_odontologia.diplomado_final.metrics.odontogram.ProcedureCountDto;
import com.app_odontologia.diplomado_final.metrics.odontogram.ProcedureTimelineItemDto;
import com.app_odontologia.diplomado_final.metrics.odontogram.ToothStatusCountDto;
import com.app_odontologia.diplomado_final.model.entity.DentalChart;
import com.app_odontologia.diplomado_final.repository.*;
import com.app_odontologia.diplomado_final.service.OdontogramMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OdontogramMetricsServiceImpl implements OdontogramMetricsService {

    private final DentalChartRepository dentalChartRepository;
    private final DentalProcedureRepository dentalProcedureRepository;
    private final ToothRepository toothRepository;
    private final ToothAttachmentRepository toothAttachmentRepository;

    @Override
    public OdontogramMetricsDto getMetrics(Long clinicId, Long patientId) {

        DentalChart chart = dentalChartRepository
                .findByClinicIdAndPatientIdAndStatus(
                        clinicId,
                        patientId,
                        DentalChart.ChartStatus.ACTIVE
                )
                .orElseThrow(() -> new IllegalStateException("Odontograma activo no encontrado"));

        Long chartId = chart.getId();

        // ===== PROCEDIMIENTOS =====
        List<Object[]> byType = dentalProcedureRepository.countProceduresByType(chartId);
        List<ProcedureCountDto> proceduresByType = byType.stream()
                .map(o -> new ProcedureCountDto(
                        (String) o[0],
                        (Long) o[1]
                ))
                .collect(Collectors.toList());

        List<Object[]> byStatus = dentalProcedureRepository.countProceduresByStatus(chartId);
        long open = 0;
        long completed = 0;

        for (Object[] o : byStatus) {
            String status = o[0].toString();
            long count = (Long) o[1];
            if ("OPEN".equals(status)) open = count;
            if ("COMPLETED".equals(status)) completed = count;
        }

        long totalProcedures = open + completed;

        // ===== DIENTES =====
        long totalTeeth = toothRepository.countTotalTeeth(chartId);
        long affectedTeeth = dentalProcedureRepository.countAffectedTeeth(chartId);
        long healthyTeeth = Math.max(0, totalTeeth - affectedTeeth);

        List<ToothStatusCountDto> teethByStatus =
                toothRepository.countTeethByStatus(chartId)
                        .stream()
                        .map(o -> new ToothStatusCountDto(
                                o[0] != null ? o[0].toString() : "UNKNOWN",
                                (Long) o[1]
                        ))
                        .collect(Collectors.toList());

        // ===== EVIDENCIA =====
        long teethWithAttachments =
                toothAttachmentRepository.countTeethWithAttachments(chartId);

        // ===== RIESGO =====
        Instant limitDate = Instant.now().minus(30, ChronoUnit.DAYS);
        long openOver30Days =
                dentalProcedureRepository.countOpenProceduresOlderThan(
                        chartId,
                        limitDate
                );

        // ===== TIMELINE =====
        List<ProcedureTimelineItemDto> timeline =
                dentalProcedureRepository.countProceduresGroupedByDate(chartId)
                        .stream()
                        .map(o -> new ProcedureTimelineItemDto(
                                ((java.sql.Date) o[0]).toLocalDate(),
                                (Long) o[1]
                        ))
                        .collect(Collectors.toList());

        return com.app_odontologia.diplomado_final.dto.odontogram.metrics.OdontogramMetricsDto.builder()
                .totalProcedures(totalProcedures)
                .openProcedures(open)
                .completedProcedures(completed)
                .proceduresByType(proceduresByType)
                .totalTeeth(totalTeeth)
                .affectedTeeth(affectedTeeth)
                .healthyTeeth(healthyTeeth)
                .teethByStatus(teethByStatus)
                .teethWithAttachments(teethWithAttachments)
                .openProceduresOver30Days(openOver30Days)
                .proceduresTimeline(timeline)
                .build();
    }
}
