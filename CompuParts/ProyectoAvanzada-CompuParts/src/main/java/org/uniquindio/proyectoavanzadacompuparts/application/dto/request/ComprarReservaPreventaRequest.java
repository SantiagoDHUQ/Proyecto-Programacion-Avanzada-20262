package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para comprar la unidad apartada por una reserva de preventa ya recibida.
 */
public record ComprarReservaPreventaRequest(
        @NotBlank(message = "El comprador es necesario") String compradorId
) {
}
