package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.util.Objects;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Value Object que representa el precio de un Componente.
 * Es inmutable: cualquier cambio de precio implica crear una nueva instancia.
 */
public record Precio(BigDecimal monto, String moneda) {

    public Precio {
        exigir(monto != null, "El monto del precio no puede ser nulo");
        exigir(moneda != null && !moneda.isBlank(), "La moneda no puede estar vacía");
        exigir(monto.signum() >= 0, "El monto del precio no puede ser negativo");
        moneda = moneda.trim().toUpperCase();
    }

    public Precio sumar(Precio otro) {
        exigir(otro != null, "No se puede sumar un precio nulo");
        exigir(this.moneda.equals(otro.moneda), "No se pueden sumar precios en distinta moneda");
        return new Precio(this.monto.add(otro.monto), this.moneda);
    }
}