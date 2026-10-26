package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.response.BuildResponse;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ResultadoCompatibilidad;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que valida si un Build es compatible, persiste el estado resultante
 * y retorna el resultado con sus motivos.
 * Requiere BuildRepository para cargar y guardar el armado.
 */
@Service
@RequiredArgsConstructor
public class ValidarCompatibilidadBuildUseCase {

    private final BuildRepository buildRepository;

    @Transactional
    public BuildResponse ejecutar(UUID buildId) {
        Build build = buildRepository.buscarPorId(buildId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Build con ese identificador"));

        ResultadoCompatibilidad resultado = build.evaluarCompatibilidad();
        buildRepository.guardar(build);

        return BuildResponse.from(build, resultado);
    }
}
