package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

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

    public MarcarComponenteAgotadoUseCase(ComponenteRepository componenteRepository, BuildRepository buildRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository, "El repositorio de componentes no puede ser nulo");
    }

    public Componente ejecutar(String componenteId) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Componente con ese identificador"));

        componente.marcarAgotado();
        return componenteRepository.guardar(componente);
    }
}