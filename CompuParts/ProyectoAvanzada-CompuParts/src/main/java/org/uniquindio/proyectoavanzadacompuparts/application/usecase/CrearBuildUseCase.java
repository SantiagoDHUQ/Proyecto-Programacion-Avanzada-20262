package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;

import java.util.Objects;

/**
 * Caso de uso que crea un nuevo Build vacío.
 * Requiere BuildRepository para persistir la entidad raíz del agregado.
 */
@Service
@RequiredArgsConstructor
public class CrearBuildUseCase {

    private final BuildRepository buildRepository;

    @Transactional
    public Build ejecutar() {
        Build build = Build.crear();
        return buildRepository.guardar(build);
    }
}