package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para que el vendedor dueño reciba una unidad que estaba en preventa.
 */
public record RecibirPreventaRequest(
        @NotBlank(message = "El vendedor es necesario") String vendedorId,
        @NotBlank(message = "El número de serie es necesario") String numeroSerie
) {
}
