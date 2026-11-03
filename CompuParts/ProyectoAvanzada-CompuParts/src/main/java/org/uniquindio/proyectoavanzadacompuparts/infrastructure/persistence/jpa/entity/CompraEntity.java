package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.embeddable.PrecioEmbeddable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "compras")
@Getter
@Setter
@NoArgsConstructor
public class CompraEntity {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "build_id")
    private UUID buildId;

    @Column(name = "comprador_id", nullable = false, length = 50)
    private String compradorId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "compra_id")
    private List<ItemCompraEntity> items = new ArrayList<>();

    @Embedded
    private PrecioEmbeddable precioCongelado;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCompra estado;
}
