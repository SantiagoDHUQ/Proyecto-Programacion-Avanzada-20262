package org.uniquindio.proyectoavanzadacompuparts.aplication.valueObject;

import java.util.Objects;

/**
 * Value Object que agrupa los datos técnicos de un Componente
 * necesarios para el Chequeo de Compatibilidad de un Build.
 */
public record EspecificacionTecnica(String socket, int wattajeRequerido, String tipoMemoria) {

    public EspecificacionTecnica {
        Objects.requireNonNull(tipoMemoria, "El tipo de memoria no puede ser nulo");
        if (wattajeRequerido < 0) {
            throw new IllegalArgumentException("El wattaje requerido no puede ser negativo");
        }
    }
}