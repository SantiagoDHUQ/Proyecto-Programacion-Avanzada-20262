package org.uniquindio.proyectoavanzadacompuparts.application.dto.response;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;

import java.time.LocalDateTime;

/**
 * DTO de salida de una reserva de preventa.
 */
public record ReservaPreventaResponse(
        String id,
        String componenteId,
        String unidadId,
        String compradorId,
        String estado,
        LocalDateTime fechaReserva
) {

    public static ReservaPreventaResponse from(ReservaPreventa reserva) {
        return new ReservaPreventaResponse(
                reserva.getId().toString(),
                reserva.getComponenteId(),
                reserva.getUnidadId().toString(),
                reserva.getCompradorId(),
                reserva.getEstado().name(),
                reserva.getFechaReserva()
        );
    }
}
