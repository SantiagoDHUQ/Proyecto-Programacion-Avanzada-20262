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

    private UnidadInventario(String numeroSerie, EstadoUnidad estado, LocalDate fechaEstimadaLlegada) {
        this.id = UUID.randomUUID();
        this.numeroSerie = numeroSerie;
        this.estado = estado;
        this.fechaEstimadaLlegada = fechaEstimadaLlegada;
    }

    public static UnidadInventario crearDisponible(String numeroSerie) {
        exigir(numeroSerie != null && !numeroSerie.isBlank(), "El número de serie es obligatorio");
        return new UnidadInventario(numeroSerie.trim(), EstadoUnidad.DISPONIBLE, null);
    }

    public static UnidadInventario crearPreventa(LocalDate fechaEstimadaLlegada) {
        exigir(fechaEstimadaLlegada != null, "La preventa exige una fecha estimada de llegada");
        exigir(!fechaEstimadaLlegada.isBefore(LocalDate.now()),
                "La fecha estimada de llegada no puede ser anterior a hoy");
        return new UnidadInventario(null, EstadoUnidad.PREVENTA, fechaEstimadaLlegada);
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
