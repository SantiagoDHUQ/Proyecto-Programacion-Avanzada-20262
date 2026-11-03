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
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoComercial;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.embeddable.EspecificacionTecnicaEmbeddable;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.embeddable.GarantiaEmbeddable;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.embeddable.PrecioEmbeddable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "componentes")
@Getter
@Setter
@NoArgsConstructor
public class ComponenteEntity {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaComponente categoria;

    @Embedded
    private EspecificacionTecnicaEmbeddable especificacion;

    @Embedded
    private PrecioEmbeddable precio;

    @Column(name = "vendedor_id", nullable = false, length = 50)
    private String vendedorId;

    @Embedded
    private GarantiaEmbeddable garantia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_comercial", nullable = false, length = 20)
    private EstadoComercial estadoComercial;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "componente_id")
    private List<UnidadInventarioEntity> unidades = new ArrayList<>();
}
