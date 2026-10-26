package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para registrar un vendedor. El tipo debe ser AUTORIZADO o PARTICULAR.
 */
public record RegistrarVendedorRequest(
        @NotBlank(message = "El identificador es necesario") String id,
        @NotBlank(message = "El nombre es necesario") String nombre,
        @NotBlank(message = "El tipo es necesario") String tipo
) {
}
