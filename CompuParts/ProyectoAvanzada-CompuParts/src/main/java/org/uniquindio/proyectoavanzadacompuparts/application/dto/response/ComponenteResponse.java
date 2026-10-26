package org.uniquindio.proyectoavanzadacompuparts.application.dto.response;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Garantia;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de salida de un Componente (SKU) con su stock y sus unidades físicas.
 */
public record ComponenteResponse(
        String id,
        String nombre,
        String categoria,
        String socket,
        int wattajeRequerido,
        String tipoMemoria,
        BigDecimal precio,
        String moneda,
        String vendedorId,
        String vendedorNombre,
        String vendedorTipo,
        Integer garantiaDias,
        String estadoComercial,
        long stockDisponible,
        long stockPreventa,
        List<UnidadInventarioResponse> unidades
) {

    public static ComponenteResponse from(Componente componente) {
        return new ComponenteResponse(
                componente.getId(),
                componente.getNombre(),
                componente.getCategoria().name(),
                componente.getEspecificacion().socket(),
                componente.getEspecificacion().wattajeRequerido(),
                componente.getEspecificacion().tipoMemoria(),
                componente.getPrecio().monto(),
                componente.getPrecio().moneda(),
                componente.getVendedor().getId(),
                componente.getVendedor().getNombre(),
                componente.getVendedor().getTipo().name(),
                componente.getGarantia().map(Garantia::dias).orElse(null),
                componente.getEstadoComercial().name(),
                componente.stockDisponible(),
                componente.stockPreventa(),
                componente.getUnidades().stream().map(UnidadInventarioResponse::from).toList()
        );
    }
}
