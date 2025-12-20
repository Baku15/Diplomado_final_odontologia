package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Representación de un diente dentro de un DentalChart.
 * Las superficies se guardan en un JSON-like map (surface -> SurfaceState),
 * para mayor flexibilidad podemos guardarlo como text (JSON) o como columnas separadas.
 *
 * Aquí lo modelamos como un Map<String, String> convertido por JPA (simple).
 */
@Entity
@Table(name = "dental_teeth", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tooth_chart_number", columnNames = {"chart_id", "tooth_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tooth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación al DentalChart
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chart_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_tooth_chart"))
    private DentalChart chart;

    // Número FDI, por ejemplo 11, 21, 36, etc.
    @Column(name = "tooth_number", nullable = false)
    private Integer toothNumber;

    // Estado general del diente
    @Enumerated(EnumType.STRING)
    @Column(name = "tooth_status", length = 50)
    private ToothStatus toothStatus;

    // Notas generales del diente
    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @OneToMany(mappedBy = "tooth", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<ToothAttachment> attachments = new java.util.ArrayList<>();

    // Representación simplificada de superficies.
    // 'surface_states' puede almacenarse como JSON en DB si tu dialecto lo soporta.
    // Aquí usamos @ElementCollection para mapear un map clave->valor.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dental_tooth_surfaces", joinColumns = @JoinColumn(name = "tooth_id"))
    @MapKeyColumn(name = "surface")
    @Column(name = "value", length = 500)
    @Builder.Default
    private Map<String, String> surfaceStates = new HashMap<>();

    public enum ToothStatus {
        SANO,
        AUSENTE,
        IMPLANTE,
        PROTESIS,
        TRATAMIENTO,
        EXTRACCION,
        // etc.
    }
}
