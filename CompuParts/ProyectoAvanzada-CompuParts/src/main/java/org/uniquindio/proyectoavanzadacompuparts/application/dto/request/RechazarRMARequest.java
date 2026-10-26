package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para que el vendedor dueño rechace una SolicitudRMA.
 */
public record RechazarRMARequest(
        @NotBlank(message = "El vendedor es necesario") String vendedorId,
        @NotBlank(message = "El motivo es necesario") String motivo
) {
}
