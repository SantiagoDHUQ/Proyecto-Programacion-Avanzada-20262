package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.Objects;

/**
 * Caso de uso para consultar un Componente (SKU) con sus unidades.
 */
@Service
@RequiredArgsConstructor
public class ConsultarComponenteUseCase {

    private final ComponenteRepository componenteRepository;

    @Transactional
    public Componente ejecutar(String componenteId) {
        return componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));
    }
}
