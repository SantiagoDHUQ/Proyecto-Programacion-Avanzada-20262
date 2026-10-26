package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que añade un Componente al Build indicado.
 * Requiere BuildRepository y ComponenteRepository para consultar ambos agregados.
 */
public class AgregarComponenteABuildUseCase {

    private final BuildRepository buildRepository;
    private final ComponenteRepository componenteRepository;

    public AgregarComponenteABuildUseCase(BuildRepository buildRepository, ComponenteRepository componenteRepository) {
        this.buildRepository = Objects.requireNonNull(buildRepository, "El repositorio de builds no puede ser nulo");
        this.componenteRepository = Objects.requireNonNull(componenteRepository, "El repositorio de componentes no puede ser nulo");
    }

    public Build ejecutar(UUID buildId, String componenteId) {
        Build build = buildRepository.buscarPorId(buildId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Build con ese identificador"));
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));
        build.agregarComponente(componente);
        return buildRepository.guardar(build);
    }
}
