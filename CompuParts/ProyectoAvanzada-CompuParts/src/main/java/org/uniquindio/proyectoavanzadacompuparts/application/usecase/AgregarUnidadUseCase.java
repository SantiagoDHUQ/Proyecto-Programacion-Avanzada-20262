package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

/**
 * Caso de uso del vendedor: agrega una unidad física (con serial) a un SKU.
 *
 * <p>Solo el vendedor dueño del componente puede agregar stock y el serial debe
 * ser único en todo el catálogo.</p>
 */
@Service
@RequiredArgsConstructor
public class AgregarUnidadUseCase {

    private final ComponenteRepository componenteRepository;

    @Transactional
    public Componente ejecutar(String componenteId, String vendedorId, String numeroSerie) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));

        boolean serieYaRegistrada = componenteRepository.existeNumeroSerie(numeroSerie);

        componente.agregarUnidad(vendedorId, numeroSerie, serieYaRegistrada);
        return componenteRepository.guardar(componente);
    }
}
