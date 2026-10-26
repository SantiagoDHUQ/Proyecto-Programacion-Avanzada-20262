package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

public enum EstadoCompra {

    PENDIENTE,
    COMPLETADA,
    CANCELADA,
    REEMBOLSADA;

    // El conocimiento de las transiciones válidas vive AQUÍ, no en ifs
    // dispersos por el código del agregado.
    public boolean puedeTransicionarA(EstadoCompra siguiente) {
        return switch (this) {
            case PENDIENTE   -> siguiente == COMPLETADA || siguiente == CANCELADA;
            case COMPLETADA  -> siguiente == REEMBOLSADA;
            case CANCELADA   -> false; // estado final: no admite salida
            case REEMBOLSADA -> false; // estado final: no admite salida
        };
    }

    public boolean esFinal() {
        return this == CANCELADA || this == REEMBOLSADA;
    }
}
