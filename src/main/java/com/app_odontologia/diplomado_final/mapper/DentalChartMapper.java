package com.app_odontologia.diplomado_final.mapper;

import com.app_odontologia.diplomado_final.dto.odontogram.DentalChartDto;
import com.app_odontologia.diplomado_final.dto.odontogram.DentalProcedureDto;
import com.app_odontologia.diplomado_final.dto.odontogram.ToothAttachmentDto;
import com.app_odontologia.diplomado_final.dto.odontogram.ToothDto;
import com.app_odontologia.diplomado_final.model.entity.DentalChart;
import com.app_odontologia.diplomado_final.model.entity.DentalProcedure;
import com.app_odontologia.diplomado_final.model.entity.Tooth;

import java.util.List;
import java.util.stream.Collectors;

public class DentalChartMapper {

    private DentalChartMapper() {}

    public static DentalChartDto toDto(DentalChart chart) {
        if (chart == null) return null;

        List<ToothDto> teeth = chart.getTeeth() == null ? List.of() :
                chart.getTeeth().stream()
                        .map(DentalChartMapper::toToothDto)
                        .collect(Collectors.toList());

        List<DentalProcedureDto> procedures = chart.getProcedures() == null ? List.of() :
                chart.getProcedures().stream()
                        .map(DentalChartMapper::toProcedureDto)
                        .collect(Collectors.toList());

        return DentalChartDto.builder()
                .id(chart.getId())
                .clinicId(chart.getClinic() != null ? chart.getClinic().getId() : null)
                .patientId(chart.getPatient() != null ? chart.getPatient().getId() : null)
                .clinicalRecordId(chart.getClinicalRecord() != null ? chart.getClinicalRecord().getId() : null)
                .version(chart.getVersion())
                .status(chart.getStatus() != null ? chart.getStatus().name() : null)
                .teeth(teeth)
                .procedures(procedures)
                .createdAt(chart.getCreatedAt())
                .updatedAt(chart.getUpdatedAt())
                .build();
    }

    public static ToothDto toToothDto(Tooth t) {
        if (t == null) return null;

        List<ToothAttachmentDto> atts = t.getAttachments() == null ? List.of() :
                t.getAttachments().stream()
                        .map(ta -> ToothAttachmentDto.builder()
                                .id(ta.getId())
                                .attachmentId(
                                        ta.getAttachment() != null
                                                ? ta.getAttachment().getId()
                                                : null
                                )
                                .filename(
                                        ta.getAttachment() != null
                                                ? ta.getAttachment().getFilename()
                                                : null
                                )
                                .storageKey(
                                        ta.getAttachment() != null
                                                ? ta.getAttachment().getStorageKey()
                                                : null
                                )
                                .contentType(
                                        ta.getAttachment() != null
                                                ? ta.getAttachment().getContentType()
                                                : null
                                )
                                .sizeBytes(
                                        ta.getAttachment() != null
                                                ? ta.getAttachment().getSizeBytes()
                                                : null
                                )
                                .notes(
                                        ta.getAttachment() != null
                                                ? ta.getAttachment().getNotes()
                                                : null
                                )
                                .createdAt(ta.getCreatedAt())
                                .build()
                        )
                        .collect(Collectors.toList());

        return ToothDto.builder()
                .id(t.getId())
                .toothNumber(t.getToothNumber())
                .toothStatus(t.getToothStatus() != null ? t.getToothStatus().name() : null)
                .notes(t.getNotes())
                .surfaceStates(t.getSurfaceStates())
                .attachments(atts)
                .build();
    }

    public static DentalProcedureDto toProcedureDto(DentalProcedure p) {
        if (p == null) return null;

        return DentalProcedureDto.builder()
                .id(p.getId())
                .toothNumber(p.getToothNumber())
                .surface(p.getSurface())
                .procedureCode(p.getProcedureCode())
                .type(p.getType())
                .description(p.getDescription())
                .performedBy(p.getPerformedBy())
                .performedAt(p.getPerformedAt())
                .createdAt(p.getCreatedAt())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .completedAt(p.getCompletedAt())
                .estimatedDurationMinutes(p.getEstimatedDurationMinutes())
                .estimatedCostCents(p.getEstimatedCostCents())
                .attachmentId(
                        p.getAttachment() != null
                                ? p.getAttachment().getId()
                                : null
                )

                //  PREPARADO PARA CONSULTA CLÍNICA
                .createdInConsultationId(p.getCreatedInConsultationId())
                .completedInConsultationId(p.getCompletedInConsultationId())

                .build();
    }
}
