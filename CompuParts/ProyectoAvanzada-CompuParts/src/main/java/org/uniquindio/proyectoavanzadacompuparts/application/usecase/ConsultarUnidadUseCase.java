package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

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
public class ConsultarUnidadUseCase {

    private final ComponenteRepository componenteRepository;

    public ConsultarUnidadUseCase(ComponenteRepository componenteRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository);
    }

    public UnidadInventario ejecutar(String componenteId, UUID unidadId) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));
        try {
            return componente.unidadPorId(unidadId);
        } catch (ReglaDominioException e) {
            throw new RecursoNoEncontradoException("No existe esa unidad en el componente");
        }
    }
}
