package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

/**
 * Estado del ciclo de vida de una SolicitudRMA.
 * El conocimiento de las transiciones válidas vive aquí, no en ifs dispersos por el agregado.
 */
public enum EstadoSolicitudRMA {

    PENDIENTE,
    ACEPTADA,
    RECHAZADA;

    public boolean puedeTransicionarA(EstadoSolicitudRMA siguiente) {
        return switch (this) {
            case PENDIENTE -> siguiente == ACEPTADA || siguiente == RECHAZADA;
            case ACEPTADA, RECHAZADA -> false; // estados finales
        };
    }

    public boolean esFinal() {
        return this != PENDIENTE;
    }
}