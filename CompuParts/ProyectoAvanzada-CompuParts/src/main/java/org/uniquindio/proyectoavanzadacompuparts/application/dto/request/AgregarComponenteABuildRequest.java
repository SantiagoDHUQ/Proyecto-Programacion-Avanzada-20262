package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para AgregarComponenteABuildUseCase.
 * El Build llega por la URL; el cuerpo solo identifica el Componente.
 */
public record AgregarComponenteABuildRequest(
        @NotBlank(message = "El identificador del componente es obligatorio")
        String componenteId) {
}
