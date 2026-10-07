package org.uniquindio.proyectoavanzadacompuparts.application.dto;

import java.util.UUID;

public record RemoverItemDePedidoRequestDTO(
    UUID pedidoId,
    String productoId
) {}
