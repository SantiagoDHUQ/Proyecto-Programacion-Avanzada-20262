package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.util.Objects;

/**
 * Especificación técnica del Componente; define el socket, consumo y tipo de memoria compatible.
 */
public record EspecificacionTecnica(String socket, int wattajeRequerido, String tipoMemoria) {

    public EspecificacionTecnica {
        Objects.requireNonNull(socket, "El socket no puede ser nulo");
        Objects.requireNonNull(tipoMemoria, "El tipo de memoria no puede ser nulo");
        if (socket.isBlank()) {
            throw new ReglaDominioException("El socket no puede estar vacío");
        }
        if (tipoMemoria.isBlank()) {
            throw new ReglaDominioException("El tipo de memoria no puede estar vacío");
        }
        if (wattajeRequerido < 0) {
            throw new ReglaDominioException("El wattaje requerido no puede ser negativo");
        }
    }
}
