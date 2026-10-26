package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO de entrada para RegistrarComponenteUseCase.
 *
 * <p>Crea el SKU del componente. Las unidades físicas se agregan después con
 * {@code POST /api/componentes/{id}/unidades}. El socket y el tipo de memoria
 * son opcionales según la categoría (por ejemplo, una PSU no tiene socket).</p>
 */
public record RegistrarComponenteRequest(

        @NotBlank(message = "El identificador es necesario")
        String id,

        @NotBlank(message = "El nombre es necesario")
        String nombre,

        @NotBlank(message = "La categoría es necesaria")
        String categoria,

        String socket,

        @Min(value = 0, message = "El wattaje no puede ser negativo")
        int wattajeRequerido,

        String tipoMemoria,

        @NotBlank(message = "La moneda es necesaria")
        String moneda,

        @NotNull(message = "El precio es necesario")
        @DecimalMin(value = "0", message = "El precio no puede ser negativo")
        BigDecimal precio,

        @Min(value = 1, message = "La garantía debe ser de al menos un día")
        Integer duracionGarantia,

        @NotBlank(message = "El vendedor es necesario")
        String vendedorId
) {
}
