package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso para consultar una unidad física de un componente.
 */
@Service
@RequiredArgsConstructor
public class ConsultarUnidadUseCase {

    private final ComponenteRepository componenteRepository;

    @Transactional(readOnly = true)
    public UnidadInventario ejecutar(String componenteId, UUID unidadId) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));
        return componente.buscarUnidad(unidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe esa unidad en el componente"));
    }
}
