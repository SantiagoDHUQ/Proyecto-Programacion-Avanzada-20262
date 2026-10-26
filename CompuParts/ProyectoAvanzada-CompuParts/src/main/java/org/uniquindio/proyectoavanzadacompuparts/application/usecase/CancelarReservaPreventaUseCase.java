package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que cancela una reserva de preventa.
 *
 * <p>Si la unidad aún no llegó, vuelve a PREVENTA. Si ya llegó y estaba
 * apartada, vuelve a DISPONIBLE.</p>
 */
public class CancelarReservaPreventaUseCase {

    private final ComponenteRepository componenteRepository;
    private final ReservaPreventaRepository reservaPreventaRepository;

    public CancelarReservaPreventaUseCase(ComponenteRepository componenteRepository,
                                          ReservaPreventaRepository reservaPreventaRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository);
        this.reservaPreventaRepository = Objects.requireNonNull(reservaPreventaRepository);
    }

    public ReservaPreventa ejecutar(UUID reservaId, String compradorId) {
        ReservaPreventa reserva = reservaPreventaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una reserva de preventa con ese identificador"));

        if (!reserva.getCompradorId().equals(compradorId)) {
            throw new ReglaDominioException("Solo el comprador que reservó puede cancelar esta reserva");
        }

        if (!reserva.estaActiva()) {
            throw new ReglaDominioException("Solo se puede cancelar una reserva de preventa activa");
        }

        Componente componente = componenteRepository.buscarPorId(reserva.getComponenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));

        componente.liberarReservaPreventa(reserva.getUnidadId());
        reserva.cancelar();

        componenteRepository.guardar(componente);
        return reservaPreventaRepository.guardar(reserva);
    }
}
