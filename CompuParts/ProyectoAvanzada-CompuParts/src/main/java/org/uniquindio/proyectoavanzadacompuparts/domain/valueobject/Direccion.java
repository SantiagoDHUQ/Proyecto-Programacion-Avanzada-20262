package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

public record Direccion(String calle, String ciudad, String codigoPostal, String pais) {

    public Direccion {
        if (calle == null || calle.isBlank()) {
            throw new ReglaDominioException("La calle no puede ser nula ni vacía");
        }
        if (ciudad == null || ciudad.isBlank()) {
            throw new ReglaDominioException("La ciudad no puede ser nula ni vacía");
        }
        if (codigoPostal == null || codigoPostal.isBlank()) {
            throw new ReglaDominioException("El código postal no puede ser nulo ni vacío");
        }
        if (pais == null || pais.isBlank()) {
            throw new ReglaDominioException("El país no puede ser nulo ni vacío");
        }
    }
}
