package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CrearBuildRequest(
        @NotEmpty(message = "Debe indicar al menos un componente para crear el Build")
        List<String> componenteIds
) {
}
