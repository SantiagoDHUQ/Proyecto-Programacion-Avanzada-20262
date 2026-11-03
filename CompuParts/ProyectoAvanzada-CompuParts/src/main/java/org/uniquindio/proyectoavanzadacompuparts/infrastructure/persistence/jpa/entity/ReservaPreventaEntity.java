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
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoReservaPreventa;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservas_preventa")
@Getter
@Setter
@NoArgsConstructor
public class ReservaPreventaEntity {

    @Id
    private UUID id;

    @Column(name = "componente_id", nullable = false, length = 50)
    private String componenteId;

    @Column(name = "unidad_id", nullable = false)
    private UUID unidadId;

    @Column(name = "comprador_id", nullable = false, length = 50)
    private String compradorId;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReservaPreventa estado;
}
