package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoSolicitudRMA;

import java.util.*;

/**
 * Implementación en memoria del puerto de persistencia del agregado SolicitudRMA.
 */
public class SolicitudRMARepositoryEnMemoria implements SolicitudRMARepository {

    private final Map<String, SolicitudRMA> almacenamiento = new HashMap<>();

    @Override
    public SolicitudRMA guardar(SolicitudRMA solicitud) {
        almacenamiento.put(solicitud.getId(), solicitud);
        return solicitud;
    }

    @Override
    public Optional<SolicitudRMA> buscarPorId(String id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }

    @Override
    public List<SolicitudRMA> listarTodos() {
        return new ArrayList<>(almacenamiento.values());
    }

    @Override
    public boolean existePendientePorComponente(String componenteId) {
        return almacenamiento.values().stream()
                .anyMatch(s -> s.getComponenteId().equals(componenteId)
                        && s.getEstado() == EstadoSolicitudRMA.PENDIENTE);
    }
}