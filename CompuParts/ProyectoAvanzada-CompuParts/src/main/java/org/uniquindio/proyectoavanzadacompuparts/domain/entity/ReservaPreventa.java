package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import lombok.Getter;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoReservaPreventa;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Reserva de una unidad en preventa hecha por un comprador.
 *
 * <p>Representa la promesa de compra: la unidad no se puede comprar ni usar en
 * un Build antes de llegar, pero queda apartada para el comprador que reservó.</p>
 */
@Getter
public class ReservaPreventa {

    private final UUID id;
    private final String componenteId;
    private final UUID unidadId;
    private final String compradorId;
    private final LocalDateTime fechaReserva;
    private EstadoReservaPreventa estado;

    private ReservaPreventa(String componenteId, UUID unidadId, String compradorId) {
        this.id = UUID.randomUUID();
        this.componenteId = componenteId;
        this.unidadId = unidadId;
        this.compradorId = compradorId;
        this.fechaReserva = LocalDateTime.now();
        this.estado = EstadoReservaPreventa.ACTIVA;
    }

    public static ReservaPreventa crear(String componenteId, UUID unidadId, String compradorId) {
        exigir(componenteId != null && !componenteId.isBlank(),
                "La reserva debe indicar el componente");
        exigir(unidadId != null, "La reserva debe indicar la unidad");
        exigir(compradorId != null && !compradorId.isBlank(),
                "La reserva debe indicar el comprador");
        return new ReservaPreventa(componenteId, unidadId, compradorId);
    }

    public void cancelar() {
        verificarTransicion(EstadoReservaPreventa.CANCELADA);
        this.estado = EstadoReservaPreventa.CANCELADA;
    }

    public void iniciarCompra() {
        verificarTransicion(EstadoReservaPreventa.EN_COMPRA);
        this.estado = EstadoReservaPreventa.EN_COMPRA;
    }

    public void volverAActiva() {
        verificarTransicion(EstadoReservaPreventa.ACTIVA);
        this.estado = EstadoReservaPreventa.ACTIVA;
    }

    public void convertir() {
        verificarTransicion(EstadoReservaPreventa.CONVERTIDA);
        this.estado = EstadoReservaPreventa.CONVERTIDA;
    }

    private void verificarTransicion(EstadoReservaPreventa siguiente) {
        if (!estado.puedeTransicionarA(siguiente)) {
            throw new ReglaDominioException("No se puede pasar de " + estado + " a " + siguiente);
        }
    }

    public boolean estaActiva() {
        return estado == EstadoReservaPreventa.ACTIVA;
    }

    public boolean estaEnCompra() {
        return estado == EstadoReservaPreventa.EN_COMPRA;
    }

    public boolean estaVigente() {
        return estado.estaVigente();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservaPreventa otra)) return false;
        return id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
