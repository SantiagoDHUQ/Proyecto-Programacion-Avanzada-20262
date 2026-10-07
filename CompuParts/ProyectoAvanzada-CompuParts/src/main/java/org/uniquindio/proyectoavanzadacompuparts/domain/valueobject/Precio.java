package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object que representa el precio de un Componente.
 * Es inmutable: cualquier cambio de precio implica crear una nueva instancia.
 */
public record Precio(BigDecimal monto, String moneda) {

    public Precio {
        if (monto.signum() < 0) {
            throw new ReglaDominioException("El monto no puede ser negativo");
        }
        if (moneda.isBlank()) {
            throw new ReglaDominioException("La moneda no puede estar vacía");
        }
    }

    public Precio sumar(Precio otro) {
        if (!this.moneda.equals(otro.moneda)) {
            throw new ReglaDominioException("No se pueden sumar precios en distinta moneda");
        }
        return new Precio(this.monto.add(otro.monto), this.moneda);
    }
}