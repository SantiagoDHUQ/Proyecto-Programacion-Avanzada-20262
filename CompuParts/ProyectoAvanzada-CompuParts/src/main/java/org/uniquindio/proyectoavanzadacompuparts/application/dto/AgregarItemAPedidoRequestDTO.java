package org.uniquindio.proyectoavanzadacompuparts.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AgregarItemAPedidoRequestDTO(
    UUID pedidoId,
    String productoId,
    int cantidad,
    BigDecimal precioMonto,
    String precioMoneda
) {}
