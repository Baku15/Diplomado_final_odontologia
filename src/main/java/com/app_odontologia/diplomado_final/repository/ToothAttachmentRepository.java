package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.ToothAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToothAttachmentRepository extends JpaRepository<ToothAttachment, Long> {

    List<ToothAttachment> findByToothIdOrderByCreatedAtDesc(Long toothId);

    Optional<ToothAttachment> findByToothIdAndAttachmentId(Long toothId, Long attachmentId);

    List<ToothAttachment> findByToothChartId(Long chartId); // puede usarse si necesitas todos del chart


    @Query("""
    SELECT COUNT(DISTINCT ta.tooth.id)
    FROM ToothAttachment ta
    WHERE ta.tooth.chart.id = :chartId
""")
    Long countTeethWithAttachments(@Param("chartId") Long chartId);


}
