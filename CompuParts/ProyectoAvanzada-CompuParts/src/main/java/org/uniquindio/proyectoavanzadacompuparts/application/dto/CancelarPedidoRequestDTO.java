package org.uniquindio.proyectoavanzadacompuparts.application.dto;

import java.util.UUID;

public record CancelarPedidoRequestDTO(
    UUID pedidoId,
    String motivo
) {}
