package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso para consultar una reserva de preventa.
 */
@Service
@RequiredArgsConstructor
public class ConsultarReservaPreventaUseCase {

    private final ReservaPreventaRepository reservaPreventaRepository;

    @Transactional
    public ReservaPreventa ejecutar(UUID reservaId) {
        return reservaPreventaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una reserva de preventa con ese identificador"));
    }
}
