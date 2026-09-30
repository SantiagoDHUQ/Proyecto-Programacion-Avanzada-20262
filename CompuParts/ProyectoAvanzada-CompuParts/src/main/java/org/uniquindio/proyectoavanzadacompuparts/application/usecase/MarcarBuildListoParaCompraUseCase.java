package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que marca un Build como listo para compra si cumple las reglas del negocio.
 * Requiere BuildRepository para obtener la entidad raíz.
 */
public class MarcarBuildListoParaCompraUseCase {

    private final BuildRepository buildRepository;

    public MarcarBuildListoParaCompraUseCase(BuildRepository buildRepository) {
        this.buildRepository = Objects.requireNonNull(buildRepository, "El repositorio de builds no puede ser nulo");
    }

    public Build ejecutar(UUID buildId) {
        Build build = buildRepository.buscarPorId(buildId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Build con ese identificador"));
        build.marcarComoListoParaCompra();
        return buildRepository.guardar(build);
    }
}
