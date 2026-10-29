package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.PreventaService;

import java.util.UUID;

/**
 * Caso de uso del comprador: reserva una unidad publicada en preventa.
 *
 * <p>La unidad queda apartada, pero no se puede comprar ni agregar a un Build
 * hasta que llegue. La reserva es una promesa de compra.</p>
 */
@Service
@RequiredArgsConstructor
public class ReservarPreventaUseCase {

    private final ComponenteRepository componenteRepository;
    private final ReservaPreventaRepository reservaPreventaRepository;
    private final PreventaService preventaService;

    @Transactional
    public ReservaPreventa ejecutar(String componenteId, UUID unidadId, String compradorId) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));

        // Hechos técnicos
        boolean hayReservaActiva = reservaPreventaRepository.buscarActivaPorUnidad(unidadId).isPresent();

        // Una llamada de dominio
        ReservaPreventa reserva = preventaService.reservar(componente, unidadId, compradorId, hayReservaActiva);

        componenteRepository.guardar(componente);
        return reservaPreventaRepository.guardar(reserva);
    }
}
