package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

/**
 * Ciclo de vida de una reserva de preventa hecha por un comprador.
 *
 * <p>La reserva es una promesa de compra: no vende la unidad, solo la aparta
 * para ese comprador. Cuando la unidad llega, queda RESERVADA a su favor.</p>
 */
public enum EstadoReservaPreventa {

    ACTIVA,
    EN_COMPRA,
    CANCELADA,
    CONVERTIDA;

    public boolean puedeTransicionarA(EstadoReservaPreventa siguiente) {
        return switch (this) {
            case ACTIVA -> siguiente == CANCELADA || siguiente == EN_COMPRA;
            case EN_COMPRA -> siguiente == CONVERTIDA || siguiente == ACTIVA;
            case CANCELADA, CONVERTIDA -> false;
        };
    }

    public boolean esFinal() {
        return this == CANCELADA || this == CONVERTIDA;
    }

    public boolean estaVigente() {
        return this == ACTIVA || this == EN_COMPRA;
    }
}
