package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Caso de uso del vendedor: publica una unidad de un componente en modalidad Preventa.
 *
 * <p>La unidad queda en PREVENTA, no se puede agregar a un Build ni comprar hasta
 * que llegue. El comprador podrá reservarla por separado.</p>
 */
@Service
@RequiredArgsConstructor
public class PublicarPreventaUseCase {

    private final ComponenteRepository componenteRepository;

    @Transactional
    public Componente ejecutar(String componenteId, String vendedorId, LocalDate fechaEstimadaLlegada) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));

        componente.validarPropietario(vendedorId);
        componente.agregarPreventa(fechaEstimadaLlegada);
        return componenteRepository.guardar(componente);
    }
}
