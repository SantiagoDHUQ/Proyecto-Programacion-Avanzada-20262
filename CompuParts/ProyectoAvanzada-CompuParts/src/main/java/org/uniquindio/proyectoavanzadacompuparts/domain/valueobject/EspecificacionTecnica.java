package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.util.Objects;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Especificación técnica del Componente; define el socket, consumo y tipo de memoria compatible.
 */
public record EspecificacionTecnica(String socket, int wattajeRequerido, String tipoMemoria) {

    public EspecificacionTecnica {
        exigir(socket != null && !socket.isBlank(), "El socket no puede estar vacío");
        exigir(tipoMemoria != null && !tipoMemoria.isBlank(), "El tipo de memoria no puede estar vacío");
        exigir(wattajeRequerido >= 0, "El wattaje requerido no puede ser negativo");
    }
}