package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso para listar el catálogo de Componentes.
 */
@Service
@RequiredArgsConstructor
public class ListarComponentesUseCase {

    private final ComponenteRepository componenteRepository;

    @Transactional
    public List<Componente> ejecutar() {
        return componenteRepository.listarTodos().stream()
                .filter(componente -> !componente.estaEliminado())
                .toList();
    }
}
