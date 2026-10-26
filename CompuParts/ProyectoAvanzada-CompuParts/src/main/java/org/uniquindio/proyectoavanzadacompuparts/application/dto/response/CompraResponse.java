package org.uniquindio.proyectoavanzadacompuparts.application.dto.response;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida de una compra con sus ítems y el total congelado.
 */
public record CompraResponse(
        String id,
        String buildId,
        String compradorId,
        String estado,
        LocalDateTime fechaCompra,
        BigDecimal total,
        String moneda,
        List<ItemCompraResponse> items
) {

    public static CompraResponse from(Compra compra) {
        return new CompraResponse(
                compra.getId(),
                compra.getBuildId() == null ? null : compra.getBuildId().toString(),
                compra.getCompradorId(),
                compra.getEstado().name(),
                compra.getFechaCompra(),
                compra.getPrecioCongelado().monto(),
                compra.getPrecioCongelado().moneda(),
                compra.getItems().stream().map(ItemCompraResponse::from).toList()
        );
    }
}
