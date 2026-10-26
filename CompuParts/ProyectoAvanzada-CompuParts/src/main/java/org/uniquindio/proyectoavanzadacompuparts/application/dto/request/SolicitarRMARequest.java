package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * DTO para que un comprador solicite una RMA sobre una unidad concreta.
 * La unidad se identifica por su número de serie dentro de la compra.
 */
public record SolicitarRMARequest(
        @NotBlank(message = "El identificador es necesario") String id,
        @NotBlank(message = "La compra es necesaria") String compraId,
        @NotBlank(message = "El número de serie es necesario") String numeroSerie,
        @NotBlank(message = "El comprador es necesario") String compradorId,
        LocalDate fechaSolicitud
) {
}
