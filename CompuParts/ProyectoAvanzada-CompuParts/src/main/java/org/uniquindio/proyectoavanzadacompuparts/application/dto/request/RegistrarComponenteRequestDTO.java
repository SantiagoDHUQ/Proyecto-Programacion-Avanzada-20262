package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;
import jakarta.validation.constraints.*;
/**
 * DTO de entrada para RegistrarComponenteUseCase.
 * mapea nombre, categoría, especificación, precio, disponibilidad y vendedor del componente.
 */
public record RegistrarComponenteRequestDTO(

        @NotBlank(message = "El identificador es necesario")
        String id,

        @NotBlank(message = "El nombre es necesario")
        String nombre,

        @NotBlank(message = "La categoría es necesaria")
        String categoria,

        @NotBlank(message = "El socket es necesario")
        String socket,

        @NotBlank(message = "El wattaje es requerido")
        @DecimalMin("1")
        int wattajeRequerido,

        @NotBlank(message = "La moneda es necesaria")
        String moneda,

        @NotBlank(message = "El precio es necesario")
        @DecimalMin("50")
        java.math.BigDecimal precio,

        @NotBlank(message = "El tipo de vendedor es necesario")
        String vendedorTipo,

        @NotBlank(message = "Se requiere un número de serie")
        String numeroSerie
) {
}