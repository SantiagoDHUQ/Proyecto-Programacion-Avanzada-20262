package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.util.Objects;

public record ItemPedido(String productoId, int cantidad, Precio precioUnitario) {

    public ItemPedido {
        Objects.requireNonNull(productoId, "El productoId no puede ser nulo");
        Objects.requireNonNull(precioUnitario, "El precioUnitario no puede ser nulo");
        if (cantidad <= 0) {
            throw new ReglaDominioException("La cantidad debe ser mayor a cero");
        }
    }

    public Precio subtotal() {
        return new Precio(precioUnitario.monto().multiply(BigDecimal.valueOf(cantidad)), precioUnitario.moneda());
    }
}
