package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

/**
 * Ciclo de vida de una unidad física de inventario.
 *
 * <p>Una unidad en PREVENTA todavía no existe físicamente y puede no tener número de serie.
 * Al recibirse pasa a DISPONIBLE, o a RESERVADA si un comprador ya la había reservado.</p>
 */
public enum EstadoUnidad {

    DISPONIBLE,
    RESERVADA,
    VENDIDA,
    PREVENTA,
    PREVENTA_RESERVADA,
    EN_RMA,
    DESCARTADA;

    public boolean estaEnPreventa() {
        return this == PREVENTA || this == PREVENTA_RESERVADA;
    }

    public boolean esFinal() {
        return this == DESCARTADA;
    }
}
