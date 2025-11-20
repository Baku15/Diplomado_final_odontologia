package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "clinic_rooms",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_clinic_room_name",
                        columnNames = {"clinic_id", "name"}
                )
        }
)
@Data
public class ClinicRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Clínica a la que pertenece este consultorio/sala.
     * Un clinic puede tener muchos rooms.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    /**
     * Nombre visible en la UI, p.ej. "Consultorio 1", "Sala RX".
     * Es único dentro de la misma clínica (ver unique constraint).
     */
    @Column(nullable = false, length = 80)
    private String name;

    /**
     * Código interno opcional, por si luego quieres un identificador corto
     * tipo "C1", "RX-01", mapeable a Location.identifier en FHIR.
     */
    @Column(length = 40)
    private String code;

    /**
     * Descripción opcional: piso, referencia, equipamiento, etc.
     */
    @Column(length = 255)
    private String description;

    /**
     * Flag para poder desactivar un consultorio sin borrarlo físicamente.
     */
    @Column(nullable = false)
    private Boolean active = true;
}
