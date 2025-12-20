package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.Tooth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToothRepository extends JpaRepository<Tooth, Long> {

    List<Tooth> findByChartId(Long chartId);

    Optional<Tooth> findByChartIdAndToothNumber(Long chartId, Integer toothNumber);
}
