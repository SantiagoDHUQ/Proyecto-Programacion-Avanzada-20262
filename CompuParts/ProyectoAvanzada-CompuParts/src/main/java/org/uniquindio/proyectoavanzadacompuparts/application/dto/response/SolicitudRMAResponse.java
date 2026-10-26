package org.uniquindio.proyectoavanzadacompuparts.application.dto.response;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;

import java.time.LocalDate;

/**
 * DTO de salida de una SolicitudRMA.
 */
public record SolicitudRMAResponse(
        String id,
        String componenteId,
        String compraId,
        String numeroSerie,
        String compradorId,
        LocalDate fechaCompra,
        int duracionGarantia,
        LocalDate fechaSolicitud,
        String estado,
        String motivoRechazo
) {

    public static SolicitudRMAResponse from(SolicitudRMA solicitud) {
        return new SolicitudRMAResponse(
                solicitud.getId(),
                solicitud.getComponenteId(),
                solicitud.getCompraId(),
                solicitud.getNumeroSerie(),
                solicitud.getCompradorId(),
                solicitud.getFechaCompra(),
                solicitud.getDuracionGarantia(),
                solicitud.getFechaSolicitud(),
                solicitud.getEstado().name(),
                solicitud.getMotivoRechazo()
        );
    }
}
