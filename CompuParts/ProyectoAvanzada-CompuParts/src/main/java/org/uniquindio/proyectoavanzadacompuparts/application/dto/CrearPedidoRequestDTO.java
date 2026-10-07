package org.uniquindio.proyectoavanzadacompuparts.application.dto;

public record CrearPedidoRequestDTO(
    String clienteId,
    String calle,
    String ciudad,
    String codigoPostal,
    String pais
) {}
