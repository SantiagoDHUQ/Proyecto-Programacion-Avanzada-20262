package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.embeddable.PrecioEmbeddable;

import java.util.UUID;

@Entity
@Table(name = "compra_items")
@Getter
@Setter
@NoArgsConstructor
public class ItemCompraEntity {

    @Id
    private UUID id;

    @Column(name = "unidad_id", nullable = false)
    private UUID unidadId;

    @Column(name = "componente_id", nullable = false, length = 50)
    private String componenteId;

    @Column(name = "numero_serie", nullable = false, length = 100)
    private String numeroSerie;

    @Embedded
    private PrecioEmbeddable precioUnitario;
}
