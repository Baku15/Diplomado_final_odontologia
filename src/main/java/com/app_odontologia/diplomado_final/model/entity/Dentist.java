package com.app_odontologia.diplomado_final.model.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "dentists")
@Data
public class Dentist {
    @Id
    private Long id; // = user_id (one-to-one)

    @MapsId
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    private String licenseNumber;   // matrícula
    private String specialty;
    private String phone;
    private String address;
}
