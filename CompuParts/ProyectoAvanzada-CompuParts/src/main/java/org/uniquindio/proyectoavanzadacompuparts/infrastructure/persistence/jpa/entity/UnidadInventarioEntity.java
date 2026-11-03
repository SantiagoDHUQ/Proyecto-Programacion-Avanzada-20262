package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "unidades_inventario")
@Getter
@Setter
@NoArgsConstructor
public class UnidadInventarioEntity {

    @Id
    private UUID id;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoUnidad estado;

    @Column(name = "fecha_estimada_llegada")
    private LocalDate fechaEstimadaLlegada;

    @Column(name = "reserva_preventa_id")
    private UUID reservaPreventaId;
}
