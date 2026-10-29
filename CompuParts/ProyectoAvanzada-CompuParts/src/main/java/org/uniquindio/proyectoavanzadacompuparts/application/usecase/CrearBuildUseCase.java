package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.factory.BuildFactory;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;

/**
 * Caso de uso que crea un nuevo Build vacío.
 * Requiere BuildRepository para persistir la entidad raíz del agregado.
 */
@Service
@RequiredArgsConstructor
public class CrearBuildUseCase {

    private final BuildRepository buildRepository;
    private final BuildFactory buildFactory;

    @Transactional
    public Build ejecutar() {
        Build build = buildFactory.nuevo();
        return buildRepository.guardar(build);
    }
}
