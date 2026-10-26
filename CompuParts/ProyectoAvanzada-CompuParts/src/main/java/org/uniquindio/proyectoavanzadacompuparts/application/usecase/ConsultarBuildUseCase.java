package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.dto.response.BuildResponse;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso de consulta: devuelve un Build por su identificador.
 * No cambia estado.
 */
public class ConsultarBuildUseCase {

    private final BuildRepository buildRepository;

    public ConsultarBuildUseCase(BuildRepository buildRepository) {
        this.buildRepository = Objects.requireNonNull(buildRepository, "El repositorio de builds no puede ser nulo");
    }

    public BuildResponse ejecutar(UUID buildId) {
        Build build = buildRepository.buscarPorId(buildId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Build con ese identificador"));
        return BuildResponse.from(build);
    }
}
