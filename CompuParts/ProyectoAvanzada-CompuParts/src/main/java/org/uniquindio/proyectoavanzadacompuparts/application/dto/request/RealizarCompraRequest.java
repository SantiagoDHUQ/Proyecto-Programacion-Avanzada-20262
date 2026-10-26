package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO para realizar la compra de un Build listo para compra.
 */
public record RealizarCompraRequest(
        @NotNull(message = "El Build es necesario") UUID buildId,
        @NotBlank(message = "El comprador es necesario") String compradorId
) {
}
