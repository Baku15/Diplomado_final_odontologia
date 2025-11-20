package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "doctor_profile")
@Data
public class DoctorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    private String specialty;
    private String phone;
    private String address;

    @Column(columnDefinition = "text")
    private String bio;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
