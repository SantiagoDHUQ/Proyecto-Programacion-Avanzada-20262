package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso del vendedor: recibe físicamente una unidad en preventa y le
 * asigna su número de serie.
 *
 * <p>Si la unidad estaba reservada por un comprador, queda RESERVADA para él;
 * si no, queda DISPONIBLE.</p>
 */
@Service
@RequiredArgsConstructor
public class RecibirPreventaUseCase {

    private final ComponenteRepository componenteRepository;

    @Transactional
    public Componente ejecutar(String componenteId, UUID unidadId, String vendedorId, String numeroSerie) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));

        boolean serieYaRegistrada = numeroSerie != null && !numeroSerie.isBlank()
                && componenteRepository.existeNumeroSerie(numeroSerie);

        componente.recibirPreventa(unidadId, vendedorId, numeroSerie, serieYaRegistrada);
        return componenteRepository.guardar(componente);
    }
}
