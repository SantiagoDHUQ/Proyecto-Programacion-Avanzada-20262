package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;

import java.util.Objects;

/**
 * Caso de uso que crea un nuevo Build vacío.
 * Requiere BuildRepository para persistir la entidad raíz del agregado.
 */
public class CrearBuildUseCase {

    private final BuildRepository buildRepository;

    public CrearBuildUseCase(BuildRepository buildRepository) {
        this.buildRepository = Objects.requireNonNull(buildRepository, "El repositorio de builds no puede ser nulo");
    }

    public Build ejecutar(String id) {
        Build build = Build.crear(id);
        return buildRepository.guardar(build);
    }
}