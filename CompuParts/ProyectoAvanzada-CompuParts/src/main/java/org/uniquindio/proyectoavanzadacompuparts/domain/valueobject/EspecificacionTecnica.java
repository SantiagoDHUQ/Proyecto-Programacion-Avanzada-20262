package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Especificación técnica del Componente; define el socket, consumo y tipo de memoria compatible.
 *
 * <p>El socket y el tipo de memoria son opcionales según la categoría: una PSU,
 * un disco o una caja no tienen socket ni tipo de RAM. Cuando vienen informados
 * se normalizan para compararlos sin distinguir mayúsculas.</p>
 */
public record EspecificacionTecnica(String socket, int wattajeRequerido, String tipoMemoria) {

    public EspecificacionTecnica {
        socket = normalizar(socket);
        tipoMemoria = normalizar(tipoMemoria);
        exigir(wattajeRequerido >= 0, "El wattaje requerido no puede ser negativo");
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase();
    }
}
