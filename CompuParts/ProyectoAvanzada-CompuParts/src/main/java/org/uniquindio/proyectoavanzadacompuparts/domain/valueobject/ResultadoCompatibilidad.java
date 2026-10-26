package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import java.util.List;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Resultado del Chequeo de Compatibilidad de un Build.
 * <p>
 * Un resultado compatible no tiene motivos; uno incompatible debe explicar al menos uno.
 * Así el caso de uso puede responder "por qué" sin depender de una excepción.
 */
public record ResultadoCompatibilidad(boolean compatible, List<String> motivos) {

    public ResultadoCompatibilidad {
        exigir(motivos != null, "Los motivos de compatibilidad no pueden ser nulos");
        motivos = List.copyOf(motivos);
        exigir(!(compatible && !motivos.isEmpty()), "Un Build compatible no puede tener motivos de incompatibilidad");
        exigir(!(!compatible && motivos.isEmpty()), "Un Build incompatible debe indicar al menos un motivo");
    }

    public static ResultadoCompatibilidad ok() {
        return new ResultadoCompatibilidad(true, List.of());
    }

    public static ResultadoCompatibilidad incompatible(List<String> motivos) {
        return new ResultadoCompatibilidad(false, motivos);
    }

    public static ResultadoCompatibilidad incompatible(String motivo) {
        return incompatible(List.of(motivo));
    }
}
