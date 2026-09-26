package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Valor monetario inmutable del Componente; se usa para evitar inconsistencias en el precio.
 */
public record Precio(BigDecimal monto, String moneda) {

    public Precio {
        Objects.requireNonNull(monto, "El monto no puede ser nulo");
        Objects.requireNonNull(moneda, "La moneda no puede ser nula");
        if (monto.signum() < 0) {
            throw new IllegalArgumentException("El monto del precio no puede ser negativo");
        }
        if (moneda.isBlank()) {
            throw new IllegalArgumentException("La moneda no puede estar vacía");
        }
    }

    public Precio sumar(Precio otro) {
        if (!this.moneda.equalsIgnoreCase(otro.moneda)) {
            throw new ReglaDominioException("No se pueden sumar precios en distinta moneda");
        }
        return new Precio(this.monto.add(otro.monto), this.moneda);
    }
}
