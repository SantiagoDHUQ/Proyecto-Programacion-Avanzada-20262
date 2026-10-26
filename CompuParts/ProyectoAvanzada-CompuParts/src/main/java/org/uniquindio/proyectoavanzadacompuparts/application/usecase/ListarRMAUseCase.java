package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso para listar las SolicitudesRMA registradas.
 */
public class ListarRMAUseCase {

    private final SolicitudRMARepository solicitudRMARepository;

    public ListarRMAUseCase(SolicitudRMARepository solicitudRMARepository) {
        this.solicitudRMARepository = Objects.requireNonNull(solicitudRMARepository);
    }

    public List<SolicitudRMA> ejecutar() {
        return solicitudRMARepository.listarTodos();
    }
}
