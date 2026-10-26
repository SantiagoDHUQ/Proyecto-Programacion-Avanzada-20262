package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import lombok.Getter;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Entidad que representa una unidad física de inventario de un Componente (SKU).
 *
 * <p>El número de serie identifica la unidad. Una unidad en preventa puede no tener
 * número de serie todavía; al recibirse se le asigna uno y pasa a estar disponible
 * (o reservada, si un comprador ya la había apartado).</p>
 */
@Getter
public class UnidadInventario {

    private final UUID id;
    private String numeroSerie;
    private EstadoUnidad estado;
    private LocalDate fechaEstimadaLlegada;
    private UUID reservaPreventaId;

    private UnidadInventario(UUID id, String numeroSerie, EstadoUnidad estado,
                             LocalDate fechaEstimadaLlegada, UUID reservaPreventaId) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.numeroSerie = numeroSerie;
        this.estado = estado;
        this.fechaEstimadaLlegada = fechaEstimadaLlegada;
        this.reservaPreventaId = reservaPreventaId;
    }

    public static UnidadInventario crearDisponible(String numeroSerie) {
        exigir(numeroSerie != null && !numeroSerie.isBlank(), "El número de serie es obligatorio");
        return new UnidadInventario(null, numeroSerie.trim(), EstadoUnidad.DISPONIBLE, null, null);
    }

    public static UnidadInventario crearPreventa(LocalDate fechaEstimadaLlegada) {
        exigir(fechaEstimadaLlegada != null, "La preventa exige una fecha estimada de llegada");
        exigir(!fechaEstimadaLlegada.isBefore(LocalDate.now()),
                "La fecha estimada de llegada no puede ser anterior a hoy");
        return new UnidadInventario(null, null, EstadoUnidad.PREVENTA, fechaEstimadaLlegada, null);
    }

    public static UnidadInventario reconstruir(UUID id, String numeroSerie, EstadoUnidad estado,
                                               LocalDate fechaEstimadaLlegada, UUID reservaPreventaId) {
        exigir(id != null, "El identificador de la unidad es obligatorio");
        exigir(estado != null, "El estado de la unidad es obligatorio");
        return new UnidadInventario(id, numeroSerie, estado, fechaEstimadaLlegada, reservaPreventaId);
    }

    /** Reserva la unidad para una compra pendiente. */
    public void reservarParaCompra() {
        exigir(estado == EstadoUnidad.DISPONIBLE, "Solo se puede reservar una unidad disponible");
        this.estado = EstadoUnidad.RESERVADA;
    }

    /** Confirma la venta de una unidad previamente reservada. */
    public void vender() {
        exigir(estado == EstadoUnidad.RESERVADA, "Solo se puede vender una unidad reservada");
        this.estado = EstadoUnidad.VENDIDA;
    }

    /** Libera una reserva de compra y devuelve la unidad al stock. */
    public void liberarReservaCompra() {
        exigir(estado == EstadoUnidad.RESERVADA, "Solo se puede liberar una unidad reservada para compra");
        // Si además hay una reserva de preventa, la unidad sigue apartada para
        // ese comprador; la reserva de preventa se libera por su propio flujo.
        if (reservaPreventaId == null) {
            this.estado = EstadoUnidad.DISPONIBLE;
        }
    }

    /** Reserva una unidad en preventa para un comprador que promete comprarla. */
    public void reservarPreventa(UUID reservaId) {
        exigir(reservaId != null, "La reserva de preventa es obligatoria");
        exigir(estado == EstadoUnidad.PREVENTA, "Solo se puede reservar una unidad en preventa");
        this.estado = EstadoUnidad.PREVENTA_RESERVADA;
        this.reservaPreventaId = reservaId;
    }

    /**
     * Cancela la reserva de preventa. Si la unidad todavía no llegó, vuelve a
     * PREVENTA; si ya llegó y estaba apartada, vuelve a DISPONIBLE.
     */
    public void liberarReservaPreventa() {
        exigir(reservaPreventaId != null, "La unidad no tiene una reserva de preventa activa");
        if (estado == EstadoUnidad.PREVENTA_RESERVADA) {
            this.estado = EstadoUnidad.PREVENTA;
        } else if (estado == EstadoUnidad.RESERVADA) {
            this.estado = EstadoUnidad.DISPONIBLE;
        } else {
            throw new ReglaDominioException("La reserva de preventa no se puede liberar en el estado actual");
        }
        this.reservaPreventaId = null;
    }

    /**
     * Recibe físicamente una unidad en preventa.
     *
     * <p>Si estaba reservada por un comprador, queda RESERVADA para él; si no,
     * pasa a DISPONIBLE para el mercado.</p>
     */
    public void recibir(String numeroSerie) {
        exigir(estado == EstadoUnidad.PREVENTA || estado == EstadoUnidad.PREVENTA_RESERVADA,
                "Solo se puede recibir una unidad en preventa");
        exigir(numeroSerie != null && !numeroSerie.isBlank(),
                "El número de serie es obligatorio al recibir la unidad");
        boolean estabaReservada = estado == EstadoUnidad.PREVENTA_RESERVADA;
        this.numeroSerie = numeroSerie.trim();
        this.fechaEstimadaLlegada = null;
        this.estado = estabaReservada ? EstadoUnidad.RESERVADA : EstadoUnidad.DISPONIBLE;
    }

    public void entrarEnRMA() {
        exigir(estado == EstadoUnidad.VENDIDA, "Solo una unidad vendida puede entrar en RMA");
        this.estado = EstadoUnidad.EN_RMA;
    }

    // ---------- Verificaciones de estado ----------

    /**
     * Regla: una unidad solo participa en una compra mientras está RESERVADA.
     * La verificación vive en la unidad para que ningún caso de uso tenga que
     * decidir por su cuenta si el inventario es coherente.
     */
    public void validarEstaReservada() {
        exigir(estado == EstadoUnidad.RESERVADA, "La unidad no está reservada");
    }

    /** Regla: solo una unidad vendida puede entrar en RMA. */
    public void validarEstaVendida() {
        exigir(estado == EstadoUnidad.VENDIDA, "La unidad no está vendida y no puede entrar en RMA");
    }

    /** Regla: una unidad en RMA es la única que puede ser reembolsada. */
    public void validarEstaEnRMA() {
        exigir(estado == EstadoUnidad.EN_RMA, "La unidad no está en RMA; primero debe aceptarse su solicitud");
    }

    /** Regla: solo una unidad publicada en preventa puede reservarse. */
    public void validarEstaEnPreventa() {
        exigir(estado == EstadoUnidad.PREVENTA, "La unidad no está disponible para reservar en preventa");
    }

    /** Regla: la unidad apartada en preventa debe ser la de esta reserva. */
    public void validarReservaPreventaCoincide(UUID reservaId) {
        exigir(reservaId != null && reservaId.equals(this.reservaPreventaId),
                "La unidad no está reservada por esta preventa");
    }

    /**
     * Valida que la unidad esté en un estado válido para comprar una reserva de preventa.
     * Debe estar RESERVADA (ya recibida) o PREVENTA_RESERVADA (reservada pero no recibida).
     * Si está en PREVENTA_RESERVADA, se lanza excepción con mensaje "aún no ha llegado".
     */
    public void validarParaComprarReservaPreventa() {
        if (estado == EstadoUnidad.PREVENTA_RESERVADA) {
            throw new ReglaDominioException("La unidad aún no ha llegado o ya no está reservada para el comprador");
        }
        exigir(estado == EstadoUnidad.RESERVADA, "La unidad no está reservada para compra");
    }

    public void rechazarRMA() {
        exigir(estado == EstadoUnidad.EN_RMA, "La unidad no está en RMA");
        this.estado = EstadoUnidad.VENDIDA;
    }

    /** Descarte definitivo: la unidad no vuelve al stock. */
    public void descartar() {
        exigir(estado != EstadoUnidad.DESCARTADA, "La unidad ya fue descartada");
        this.estado = EstadoUnidad.DESCARTADA;
        this.fechaEstimadaLlegada = null;
        this.reservaPreventaId = null;
    }

    public boolean estaDisponible() {
        return estado == EstadoUnidad.DISPONIBLE;
    }

    public boolean estaReservada() {
        return estado == EstadoUnidad.RESERVADA;
    }

    public boolean estaVendida() {
        return estado == EstadoUnidad.VENDIDA;
    }

    public boolean estaEnPreventa() {
        return estado.estaEnPreventa();
    }

    public boolean estaDescartada() {
        return estado == EstadoUnidad.DESCARTADA;
    }

    public boolean tieneNumeroSerie() {
        return numeroSerie != null && !numeroSerie.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnidadInventario unidad)) return false;
        return id.equals(unidad.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
