package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import java.util.UUID;

/**
 * DTO de entrada para AgregarComponenteABuildUseCase.
 * contiene la referencia del Build y el Componente que se desea incorporar.
 */
public record AgregarComponenteABuildRequestDTO(String buildId, String componenteId) {
}