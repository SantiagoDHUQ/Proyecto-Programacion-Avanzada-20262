package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso del comprador: reserva una unidad publicada en preventa.
 *
 * <p>La unidad queda apartada, pero no se puede comprar ni agregar a un Build
 * hasta que llegue. La reserva es una promesa de compra.</p>
 */
public class ReservarPreventaUseCase {

    private final ComponenteRepository componenteRepository;
    private final ReservaPreventaRepository reservaPreventaRepository;

    public ReservarPreventaUseCase(ComponenteRepository componenteRepository,
                                   ReservaPreventaRepository reservaPreventaRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository);
        this.reservaPreventaRepository = Objects.requireNonNull(reservaPreventaRepository);
    }

    public ReservaPreventa ejecutar(String componenteId, UUID unidadId, String compradorId) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));

        UnidadInventario unidad = componente.unidadPorId(unidadId);
        if (unidad.getEstado() != EstadoUnidad.PREVENTA) {
            throw new ReglaDominioException("La unidad no está disponible para reservar en preventa");
        }
        if (reservaPreventaRepository.buscarActivaPorUnidad(unidadId).isPresent()) {
            throw new ReglaDominioException("La unidad ya tiene una reserva de preventa activa");
        }

        ReservaPreventa reserva = ReservaPreventa.crear(componenteId, unidadId, compradorId);
        componente.reservarPreventa(unidadId, reserva.getId());
        componenteRepository.guardar(componente);
        return reservaPreventaRepository.guardar(reserva);
    }
}
