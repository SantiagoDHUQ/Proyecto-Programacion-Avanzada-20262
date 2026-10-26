package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para que el comprador reembolse una compra completada cuyas unidades
 * ya fueron aceptadas en RMA.
 */
public record ReembolsarCompraRequest(
        @NotBlank(message = "El comprador es necesario") String compradorId,
        @NotBlank(message = "El motivo es necesario") String motivo
) {
}
