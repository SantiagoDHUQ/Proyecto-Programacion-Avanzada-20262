package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.dto.AgregarComponenteABuildRequestDTO;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.Objects;

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

    public Build ejecutar(AgregarComponenteABuildRequestDTO request) {
        Build build = buildRepository.buscarPorId(request.buildId())
                .orElseThrow(() -> new ReglaDominioException("No existe un Build con ese identificador"));
        Componente componente = componenteRepository.buscarPorId(request.componenteId())
                .orElseThrow(() -> new ReglaDominioException("No existe un Componente con ese identificador"));
        build.agregarComponente(componente);
        return buildRepository.guardar(build);
    }
}
