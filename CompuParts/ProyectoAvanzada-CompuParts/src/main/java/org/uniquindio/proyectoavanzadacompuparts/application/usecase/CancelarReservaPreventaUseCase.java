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
 * Caso de uso que cancela una reserva de preventa.
 *
 * <p>Si la unidad aún no llegó, vuelve a PREVENTA. Si ya llegó y estaba
 * apartada, vuelve a DISPONIBLE.</p>
 */
@Service
@RequiredArgsConstructor
public class CancelarReservaPreventaUseCase {

    private final ComponenteRepository componenteRepository;
    private final ReservaPreventaRepository reservaPreventaRepository;
    private final PreventaService preventaService;

    @Transactional
    public ReservaPreventa ejecutar(UUID reservaId, String compradorId) {
        ReservaPreventa reserva = reservaPreventaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una reserva de preventa con ese identificador"));

        Componente componente = componenteRepository.buscarPorId(reserva.getComponenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));

        // Una llamada de dominio
        preventaService.cancelar(reserva, componente, compradorId);

        componenteRepository.guardar(componente);
        return reservaPreventaRepository.guardar(reserva);
    }
}
