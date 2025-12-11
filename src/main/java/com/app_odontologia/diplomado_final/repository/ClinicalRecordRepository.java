    package com.app_odontologia.diplomado_final.repository;

    import com.app_odontologia.diplomado_final.model.entity.ClinicalRecord;
    import com.app_odontologia.diplomado_final.model.entity.ClinicalRecord.ClinicalRecordStatus;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.Optional;

    @Repository
    public interface ClinicalRecordRepository extends JpaRepository<ClinicalRecord, Long> {

        Optional<ClinicalRecord> findByClinicIdAndPatientIdAndStatus(
                Long clinicId,
                Long patientId,
                ClinicalRecordStatus status
        );

        Optional<ClinicalRecord> findByPatientIdAndStatus(
                Long patientId,
                ClinicalRecordStatus status
        );
    }
