package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.springframework.cglib.core.Local;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.util.Objects;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Garantía del fabricante: solo la DURACIÓN en días.
 * La fecha de compra pertenece a la Compra, por eso se recibe al consultar la vigencia.
 */
public record Garantia(int dias) {

    public Garantia {
        exigir(dias > 0, "La duración de la garantía debe ser positiva");
    }

    /** Regla 3: vigente solo si hoy es ANTERIOR a fechaCompra + dias. */
    public boolean estaVigente(LocalDate fechaCompra, LocalDate hoy) {
        exigir(fechaCompra != null, "La fecha de compra es obligatoria");
        exigir(hoy != null, "La fecha actual es obligatoria");
        return hoy.isBefore(fechaCompra.plusDays(dias));
    }
}