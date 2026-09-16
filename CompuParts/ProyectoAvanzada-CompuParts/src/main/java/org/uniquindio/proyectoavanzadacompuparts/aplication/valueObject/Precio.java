package org.uniquindio.proyectoavanzadacompuparts.aplication.valueObject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object que representa el precio de un Componente.
 * Es inmutable: cualquier cambio de precio implica crear una nueva instancia.
 */
public record Precio(BigDecimal monto, String moneda) {

    public Precio {
        Objects.requireNonNull(monto, "El monto no puede ser nulo");
        Objects.requireNonNull(moneda, "La moneda no puede ser nula");
        if (monto.signum() < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        if (moneda.isBlank()) {
            throw new IllegalArgumentException("La moneda no puede estar vacía");
        }
    }

    public Precio sumar(Precio otro) {
        if (!this.moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException("No se pueden sumar precios en distinta moneda");
        }
        return new Precio(this.monto.add(otro.monto), this.moneda);
    }
}