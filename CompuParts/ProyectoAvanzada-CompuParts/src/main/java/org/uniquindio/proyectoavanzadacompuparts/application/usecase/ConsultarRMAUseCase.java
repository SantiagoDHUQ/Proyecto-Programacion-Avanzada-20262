package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;

import java.util.Objects;

/**
 * Caso de uso para consultar una SolicitudRMA por su identificador.
 */
@Service
@RequiredArgsConstructor
public class ConsultarRMAUseCase {

    private final SolicitudRMARepository solicitudRMARepository;

    @Transactional
    public SolicitudRMA ejecutar(String solicitudId) {
        return solicitudRMARepository.buscarPorId(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una SolicitudRMA con ese identificador"));
    }
}
