package org.uniquindio.proyectoavanzadacompuparts.domain.service;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Servicio de dominio para operaciones de preventa que cruzan agregados.
 * <p>
 * Encapsula la lógica de reserva y cancelación de preventas coordinando
 * Componente, ReservaPreventa y UnidadInventario.
 * <p>
 * Implementa "verificar todo, luego aplicar todo" para atomicidad.
 */
public final class PreventaService {

    private final ReservaPreventaRepository reservaPreventaRepository;
    private final Clock clock;

    public PreventaService(ReservaPreventaRepository reservaPreventaRepository, Clock clock) {
        this.reservaPreventaRepository = reservaPreventaRepository;
        this.clock = clock;
    }

    /**
     * Reserva una unidad en preventa para un comprador.
     * <p>
     * Verifica todo antes de mutar:
     * - La unidad está en estado PREVENTA
     * - No hay reserva activa para esa unidad
     * Luego crea la reserva y reserva la unidad en el componente.
     *
     * @return la reserva creada
     */
    public ReservaPreventa reservar(Componente componente, UUID unidadId, String compradorId,
                                    boolean hayReservaActiva) {
        // --- VERIFICAR TODO ---
        UnidadInventario unidad = componente.unidadPorId(unidadId);
        unidad.validarEstaEnPreventa();
        exigir(!hayReservaActiva, "La unidad ya tiene una reserva de preventa activa");

        // --- APLICAR TODO ---
        LocalDateTime ahora = LocalDateTime.now(clock);
        ReservaPreventa reserva = ReservaPreventa.crear(componente.getId(), unidadId, compradorId);
        componente.reservarPreventa(unidadId, reserva.getId());
        return reserva;
    }

    /**
     * Valida que el comprador sea el dueño de la reserva.
     */
    public void validarDueno(UUID reservaId, String compradorId) {
        ReservaPreventa reserva = reservaPreventaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new ReglaDominioException("No existe una reserva de preventa con ese identificador"));
        reserva.validarDueno(compradorId);
    }

    /**
     * Cancela una reserva de preventa.
     * <p>
     * Valida que el comprador sea el dueño y la reserva esté activa.
     * Libera la reserva en la unidad y cambia el estado de la reserva a CANCELADA.
     */
    public void cancelar(ReservaPreventa reserva, Componente componente, String compradorId) {
        // Validar propiedad
        reserva.validarDueno(compradorId);
        reserva.validarCancelar();

        // Liberar la reserva en la unidad
        componente.liberarReservaPreventa(reserva.getUnidadId());

        // Cancelar la reserva
        reserva.cancelar();
    }

    /**
     * Convierte una reserva de preventa en compra (cuando la unidad llega).
     * Valida que el comprador sea el dueño y la reserva esté activa.
     * Cambia la reserva a EN_COMPRA y reserva la unidad para compra.
     */
    public void iniciarCompra(ReservaPreventa reserva, Componente componente, String compradorId) {
        reserva.validarDueno(compradorId);
        reserva.validarIniciarCompra();

        UnidadInventario unidad = componente.unidadPorId(reserva.getUnidadId());
        unidad.validarParaComprarReservaPreventa();
        unidad.validarReservaPreventaCoincide(reserva.getId());

        reserva.iniciarCompra();
    }

    /**
     * Vuelve la reserva a ACTIVA (cuando se cancela la compra asociada).
     */
    public void volverAActiva(ReservaPreventa reserva) {
        reserva.volverAActiva();
    }

    /**
     * Convierte la reserva a CONVERTIDA (cuando se confirma la compra).
     */
    public void convertir(ReservaPreventa reserva) {
        reserva.convertir();
    }
}