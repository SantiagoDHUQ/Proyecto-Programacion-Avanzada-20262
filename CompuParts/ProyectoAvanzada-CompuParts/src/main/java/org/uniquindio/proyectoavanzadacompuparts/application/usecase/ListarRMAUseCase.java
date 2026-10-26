package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso para listar las SolicitudesRMA registradas.
 */
@Service
@RequiredArgsConstructor
public class ListarRMAUseCase {

    private final SolicitudRMARepository solicitudRMARepository;

    @Transactional
    public List<SolicitudRMA> ejecutar() {
        return solicitudRMARepository.listarTodos();
    }
}
