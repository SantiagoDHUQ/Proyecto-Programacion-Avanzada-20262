package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que elimina un componente revisando que no haga parte de un Build activo
 */

public class EliminarComponenteUseCase {

    private final ComponenteRepository componenteRepository;
    private final BuildRepository buildRepository;

    public EliminarComponenteUseCase(ComponenteRepository componenteRepository, BuildRepository buildRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository);
        this.buildRepository = Objects.requireNonNull(buildRepository);
    }

    public void ejecutar(String componenteId, UUID buildId) {
        Build build = buildRepository.buscarPorId(buildId)
                .orElseThrow(()-> new ReglaDominioException("No existe un build con ese ID"));
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(()-> new ReglaDominioException("No existe un componente con ese ID"));
        if (componente.esActivoEnBuild(build)) {
            throw new ReglaDominioException("El componente pertenece a un Build activo");
        }
        build.eliminarComponente(componenteId);
    }
}
