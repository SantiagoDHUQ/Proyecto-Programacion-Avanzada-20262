package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que marca un Build como listo para compra si cumple las reglas del negocio.
 * Requiere BuildRepository para obtener la entidad raíz.
 */
@Service
@RequiredArgsConstructor
public class MarcarBuildListoParaCompraUseCase {

    private final BuildRepository buildRepository;

    @Transactional
    public Build ejecutar(UUID buildId) {
        Build build = buildRepository.buscarPorId(buildId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Build con ese identificador"));
        build.marcarComoListoParaCompra();
        return buildRepository.guardar(build);
    }
}