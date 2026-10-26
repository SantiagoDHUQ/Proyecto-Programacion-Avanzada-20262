package org.uniquindio.proyectoavanzadacompuparts.application.dto.response;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;

/**
 * DTO de salida de un vendedor.
 */
public record VendedorResponse(
        String id,
        String nombre,
        String tipo
) {

    public static VendedorResponse from(Vendedor vendedor) {
        return new VendedorResponse(
                vendedor.getId(),
                vendedor.getNombre(),
                vendedor.getTipo().name()
        );
    }
}
