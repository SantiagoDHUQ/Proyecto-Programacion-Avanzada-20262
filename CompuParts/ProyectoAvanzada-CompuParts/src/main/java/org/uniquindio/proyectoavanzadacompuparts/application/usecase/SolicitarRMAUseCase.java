package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que solicita una RMA para un Componente dentro de garantía.
 * Requiere ComponenteRepository para validar la entidad referenciada.
 */
public class SolicitarRMAUseCase {

    private final ComponenteRepository componenteRepository;

    public SolicitarRMAUseCase(ComponenteRepository componenteRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository, "El repositorio de componentes no puede ser nulo");
    }

    public SolicitudRMA ejecutar(UUID componenteId, String numeroSerie) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Componente con ese identificador"));
        return SolicitudRMA.crear(componente, numeroSerie, LocalDate.now());
    }
}
