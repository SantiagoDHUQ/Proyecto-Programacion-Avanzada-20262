package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que marca un Componente como agotado si no participa en un Build activo.
 * Requiere ComponenteRepository y BuildRepository para verificar la regla de dominio.
 */
public class MarcarComponenteAgotadoUseCase {

    private final ComponenteRepository componenteRepository;
    private final BuildRepository buildRepository;

    public MarcarComponenteAgotadoUseCase(ComponenteRepository componenteRepository, BuildRepository buildRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository, "El repositorio de componentes no puede ser nulo");
        this.buildRepository = Objects.requireNonNull(buildRepository, "El repositorio de builds no puede ser nulo");
    }

    public Componente ejecutar(UUID componenteId) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Componente con ese identificador"));

        boolean estaEnBuildActivo = buildRepository.listarTodos().stream()
                .flatMap(build -> build.getComponentes().stream())
                .anyMatch(c -> c.equals(componente));

        if (estaEnBuildActivo) {
            throw new ReglaDominioException("No se puede eliminar un Componente que forma parte de un Build activo; solo se puede marcar como AGOTADO");
        }

        componente.marcarAgotado();
        return componenteRepository.guardar(componente);
    }
}
