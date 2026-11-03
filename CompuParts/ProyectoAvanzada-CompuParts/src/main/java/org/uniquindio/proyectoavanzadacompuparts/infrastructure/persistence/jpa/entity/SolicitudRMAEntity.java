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
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoSolicitudRMA;

import java.time.LocalDate;

@Entity
@Table(name = "solicitudes_rma")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudRMAEntity {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "componente_id", nullable = false, length = 50)
    private String componenteId;

    @Column(name = "compra_id", nullable = false, length = 50)
    private String compraId;

    @Column(name = "numero_serie", nullable = false, length = 100)
    private String numeroSerie;

    @Column(name = "comprador_id", nullable = false, length = 50)
    private String compradorId;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra;

    @Column(name = "duracion_garantia", nullable = false)
    private int duracionGarantia;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitudRMA estado;

    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;
}
