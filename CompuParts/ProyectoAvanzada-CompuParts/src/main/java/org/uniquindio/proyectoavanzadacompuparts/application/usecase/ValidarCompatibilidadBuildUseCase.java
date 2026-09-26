package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.dto.BuildResponseDTO;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que valida si un Build es compatible y retorna su estado.
 * Requiere BuildRepository para inspeccionar y persistir el armado.
 */
public class ValidarCompatibilidadBuildUseCase {

    private final BuildRepository buildRepository;

    public ValidarCompatibilidadBuildUseCase(BuildRepository buildRepository) {
        this.buildRepository = Objects.requireNonNull(buildRepository, "El repositorio de builds no puede ser nulo");
    }

    public BuildResponseDTO ejecutar(UUID buildId) {
        Build build = buildRepository.buscarPorId(buildId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Build con ese identificador"));
        try {
            build.validarCompatibilidad();
            return new BuildResponseDTO(build.getId(), EstadoBuild.EN_CONSTRUCCION, build.getComponentes(), true);
        } catch (ReglaDominioException e) {
            return new BuildResponseDTO(build.getId(), EstadoBuild.INCOMPATIBLE, build.getComponentes(), false);
        }
    }
}
