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
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;

@Entity
@Table(name = "vendedores")
@Getter
@Setter
@NoArgsConstructor
public class VendedorEntity {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Vendedor.TipoVendedor tipo;
}
