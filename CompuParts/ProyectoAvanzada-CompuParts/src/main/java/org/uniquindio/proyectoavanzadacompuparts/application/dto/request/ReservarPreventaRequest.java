package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para que un comprador reserve una unidad publicada en preventa.
 */
public record ReservarPreventaRequest(
        @NotBlank(message = "El comprador es necesario") String compradorId
) {
}
