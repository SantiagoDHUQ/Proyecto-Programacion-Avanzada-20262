package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para operaciones de vendedor que solo necesitan identificarse.
 */
public record OperacionVendedorRequest(
        @NotBlank(message = "El vendedor es necesario") String vendedorId
) {
}
