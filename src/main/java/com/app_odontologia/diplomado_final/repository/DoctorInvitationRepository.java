package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.DoctorInvitation;
import com.app_odontologia.diplomado_final.model.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorInvitationRepository extends JpaRepository<DoctorInvitation, Long> {

    Optional<DoctorInvitation> findByToken(String token);

    boolean existsByClinic_IdAndEmailAndStatusIn(
            Long clinicId,
            String email,
            Iterable<InvitationStatus> statuses
    );

    Optional<DoctorInvitation> findTopByClinic_IdAndEmailIgnoreCaseOrderByCreatedAtDesc(
            Long clinicId,
            String email
    );


}
