package org.uniquindio.proyectoavanzadacompuparts.application.dto.response;

import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.math.BigDecimal;

/**
 * DTO de salida de un ítem de compra (una unidad concreta).
 */
public record ItemCompraResponse(
        String unidadId,
        String componenteId,
        String numeroSerie,
        BigDecimal precioUnitario,
        String moneda
) {

    public static ItemCompraResponse from(ItemCompra item) {
        return new ItemCompraResponse(
                item.unidadId().toString(),
                item.componenteId(),
                item.numeroSerie(),
                item.precioUnitario().monto(),
                item.precioUnitario().moneda()
        );
    }
}
