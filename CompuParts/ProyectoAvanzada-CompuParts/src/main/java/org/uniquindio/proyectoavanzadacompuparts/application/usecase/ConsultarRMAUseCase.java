package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;

import java.util.Objects;

/**
 * Caso de uso para consultar una SolicitudRMA por su identificador.
 */
public class ConsultarRMAUseCase {

    private final SolicitudRMARepository solicitudRMARepository;

    public ConsultarRMAUseCase(SolicitudRMARepository solicitudRMARepository) {
        this.solicitudRMARepository = Objects.requireNonNull(solicitudRMARepository);
    }

    public SolicitudRMA ejecutar(String solicitudId) {
        return solicitudRMARepository.buscarPorId(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una SolicitudRMA con ese identificador"));
    }
}
