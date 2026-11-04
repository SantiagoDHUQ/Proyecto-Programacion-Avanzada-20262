package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.dto.VentasPorVendedorDTO;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.CompraEntity;

import java.util.List;

public interface CompraJpaRepository extends JpaRepository<CompraEntity, String> {

    /**
     * Reporte complejo que calcula las ventas totales agrupadas por vendedor
     * utilizando JOIN entre CompraEntity, sus items y ComponenteEntity, GROUP BY y
     * la función SUM.
     */
    @Query("""
                SELECT new org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.dto.VentasPorVendedorDTO(
                    comp.vendedorId,
                    SUM(item.precioUnitario.monto)
                )
                FROM CompraEntity c
                JOIN c.items item
                JOIN ComponenteEntity comp ON item.componenteId = comp.id
                WHERE c.estado = org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra.COMPLETADA
                GROUP BY comp.vendedorId
            """)
    List<VentasPorVendedorDTO> calcularVentasTotalesPorVendedor();

}
