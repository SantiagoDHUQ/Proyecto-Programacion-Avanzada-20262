package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que crea una reserva del componente en modalidad Preventa.
 * Requiere ComponenteRepository para localizar la pieza y validar la regla del negocio.
 */
public class CrearReservaPreventaUseCase {

    private final ComponenteRepository componenteRepository;

    public CrearReservaPreventaUseCase(ComponenteRepository componenteRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository, "El repositorio de componentes no puede ser nulo");
    }

    public Componente ejecutar(UUID componenteId, LocalDate fechaEstimadaLlegada) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Componente con ese identificador"));
        componente.pasarAPreventa(fechaEstimadaLlegada);
        return componenteRepository.guardar(componente);
    }
}
