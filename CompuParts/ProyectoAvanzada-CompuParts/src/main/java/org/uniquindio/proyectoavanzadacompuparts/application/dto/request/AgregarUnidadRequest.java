package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para agregar una unidad física a un SKU. El vendedor debe ser su dueño.
 */
public record AgregarUnidadRequest(
        @NotBlank(message = "El vendedor es necesario") String vendedorId,
        @NotBlank(message = "El número de serie es necesario") String numeroSerie
) {
}
