package org.uniquindio.proyectoavanzadacompuparts.application.dto;

/**
 * DTO de entrada para RegistrarComponenteUseCase.
 * mapea nombre, categoría, especificación, precio, disponibilidad y vendedor del componente.
 */
public record RegistrarComponenteRequestDTO(
        String nombre,
        String categoria,
        String socket,
        int wattajeRequerido,
        String tipoMemoria,
        String moneda,
        java.math.BigDecimal precio,
        String vendedorTipo,
        String numeroSerie
) {
}
