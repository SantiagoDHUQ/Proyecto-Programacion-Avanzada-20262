package org.uniquindio.proyectoavanzadacompuparts.application.dto.response;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;

import java.time.LocalDate;

/**
 * DTO de salida de una unidad física del inventario.
 */
public record UnidadInventarioResponse(
        String id,
        String numeroSerie,
        String estado,
        LocalDate fechaEstimadaLlegada,
        String reservaPreventaId
) {

    public static UnidadInventarioResponse from(UnidadInventario unidad) {
        return new UnidadInventarioResponse(
                unidad.getId().toString(),
                unidad.getNumeroSerie(),
                unidad.getEstado().name(),
                unidad.getFechaEstimadaLlegada(),
                unidad.getReservaPreventaId() == null ? null : unidad.getReservaPreventaId().toString()
        );
    }
}
