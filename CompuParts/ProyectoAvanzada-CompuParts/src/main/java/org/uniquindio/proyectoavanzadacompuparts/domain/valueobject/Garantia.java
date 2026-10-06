package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.springframework.cglib.core.Local;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Garantía asociada a la compra del Componente, con fecha de compra y duración en días.
 */
public record Garantia(LocalDate fechaCompra, Integer duracionGarantia) {

    public Garantia {
        Objects.requireNonNull(fechaCompra, "La fecha de compra no puede ser nula");
        if (duracionGarantia <= 0) {
            throw new ReglaDominioException("La duración de la garantía debe ser positiva");
        }
    }

    public boolean estaVigente() {
        LocalDate fechaActual = LocalDate.now();
        return fechaActual.isBefore(fechaCompra.plusDays(duracionGarantia));
    }
}