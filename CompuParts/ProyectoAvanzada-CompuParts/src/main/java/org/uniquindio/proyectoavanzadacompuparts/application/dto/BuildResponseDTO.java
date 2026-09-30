package org.uniquindio.proyectoavanzadacompuparts.application.dto;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;

import java.util.List;
import java.util.UUID;

/**
 * DTO de salida para la respuesta del caso de uso de validación de Build.
 * expone el estado, los componentes y si el armado quedó listo para compra.
 */
public record BuildResponseDTO(UUID buildId, EstadoBuild estado, List<Componente> componentes, boolean compatible) {
}
