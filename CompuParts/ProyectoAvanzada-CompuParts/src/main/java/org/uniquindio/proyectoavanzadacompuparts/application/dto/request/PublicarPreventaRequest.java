package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO para publicar una unidad en preventa. Solo un vendedor autorizado puede hacerlo.
 */
public record PublicarPreventaRequest(
        @NotBlank(message = "El vendedor es necesario") String vendedorId,
        @NotNull(message = "La fecha estimada de llegada es necesaria")
        @Future(message = "La fecha estimada debe ser futura")
        LocalDate fechaEstimadaLlegada
) {
}
