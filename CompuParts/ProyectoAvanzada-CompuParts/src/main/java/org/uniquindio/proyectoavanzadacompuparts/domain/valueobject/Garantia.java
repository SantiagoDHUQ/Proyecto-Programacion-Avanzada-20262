package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Garantía asociada a la compra del Componente, con fecha de compra y duración en días.
 */
public record Garantia(LocalDate fechaCompra, int duracionGarantia) {

    public Garantia {
        Objects.requireNonNull(fechaCompra, "La fecha de compra no puede ser nula");
        if (duracionGarantia <= 0) {
            throw new ReglaDominioException("La duración de la garantía debe ser positiva");
        }
    }

    public boolean estaVigente(LocalDate fechaActual) {
        Objects.requireNonNull(fechaActual, "La fecha actual no puede ser nula");
        return !fechaActual.isAfter(fechaCompra.plusDays(duracionGarantia));
    }
}
