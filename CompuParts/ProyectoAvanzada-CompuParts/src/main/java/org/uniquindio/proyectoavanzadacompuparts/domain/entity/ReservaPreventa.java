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

    private ReservaPreventa(UUID id, String componenteId, UUID unidadId, String compradorId,
                            LocalDateTime fechaReserva, EstadoReservaPreventa estado) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.componenteId = componenteId;
        this.unidadId = unidadId;
        this.compradorId = compradorId;
        this.fechaReserva = fechaReserva;
        this.estado = estado;
    }

    public static ReservaPreventa crear(String componenteId, UUID unidadId, String compradorId) {
        exigir(componenteId != null && !componenteId.isBlank(),
                "La reserva debe indicar el componente");
        exigir(unidadId != null, "La reserva debe indicar la unidad");
        exigir(compradorId != null && !compradorId.isBlank(),
                "La reserva debe indicar el comprador");
        return new ReservaPreventa(null, componenteId, unidadId, compradorId,
                LocalDateTime.now(), EstadoReservaPreventa.ACTIVA);
    }

    public static ReservaPreventa reconstruir(UUID id, String componenteId, UUID unidadId,
                                              String compradorId, LocalDateTime fechaReserva,
                                              EstadoReservaPreventa estado) {
        exigir(id != null, "El identificador de la reserva es obligatorio");
        exigir(componenteId != null && !componenteId.isBlank(),
                "La reserva debe indicar el componente");
        exigir(unidadId != null, "La reserva debe indicar la unidad");
        exigir(compradorId != null && !compradorId.isBlank(),
                "La reserva debe indicar el comprador");
        exigir(fechaReserva != null, "La fecha de reserva es obligatoria");
        exigir(estado != null, "El estado de la reserva es obligatorio");
        return new ReservaPreventa(id, componenteId, unidadId, compradorId, fechaReserva, estado);
    }

    public void cancelar() {
        validarCancelar();
        verificarTransicion(EstadoReservaPreventa.CANCELADA);
        this.estado = EstadoReservaPreventa.CANCELADA;
    }

    public void iniciarCompra() {
        validarIniciarCompra();
        verificarTransicion(EstadoReservaPreventa.EN_COMPRA);
        this.estado = EstadoReservaPreventa.EN_COMPRA;
    }

    public void volverAActiva() {
        validarVolverAActiva();
        verificarTransicion(EstadoReservaPreventa.ACTIVA);
        this.estado = EstadoReservaPreventa.ACTIVA;
    }

    public void convertir() {
        validarConvertir();
        verificarTransicion(EstadoReservaPreventa.CONVERTIDA);
        this.estado = EstadoReservaPreventa.CONVERTIDA;
    }

    /**
     * Valida que la reserva pueda ser cancelada.
     */
    public void validarCancelar() {
        exigir(estaActiva(), "Solo se puede cancelar una reserva de preventa activa");
    }

    /**
     * Valida que la reserva pueda iniciar una compra.
     */
    public void validarIniciarCompra() {
        exigir(!estaEnCompra(), "La reserva ya tiene una compra pendiente");
        exigir(estaActiva(), "La reserva de preventa no está activa");
    }

    /**
     * Valida que la reserva pueda volver a ACTIVA.
     */
    public void validarVolverAActiva() {
        exigir(estaEnCompra(), "Solo se puede volver a ACTIVA desde EN_COMPRA");
    }

    /**
     * Valida que la reserva pueda ser convertida.
     */
    public void validarConvertir() {
        exigir(estaEnCompra(), "Solo se puede convertir una reserva en EN_COMPRA");
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
