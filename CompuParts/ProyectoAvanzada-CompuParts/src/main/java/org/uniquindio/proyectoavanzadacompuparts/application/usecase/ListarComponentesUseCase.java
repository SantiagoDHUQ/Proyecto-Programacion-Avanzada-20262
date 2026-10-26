package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso para listar el catálogo de Componentes.
 */
public class ListarComponentesUseCase {

    private final ComponenteRepository componenteRepository;

    public ListarComponentesUseCase(ComponenteRepository componenteRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository);
    }

    public List<Componente> ejecutar() {
        return componenteRepository.listarTodos().stream()
                .filter(componente -> !componente.estaEliminado())
                .toList();
    }
}
