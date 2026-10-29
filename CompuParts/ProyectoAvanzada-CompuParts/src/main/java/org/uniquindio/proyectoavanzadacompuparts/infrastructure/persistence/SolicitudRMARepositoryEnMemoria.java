package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoSolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

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
    public boolean existePendientePorNumeroSerie(String numeroSerie) {
        if (numeroSerie == null || numeroSerie.isBlank()) {
            return false;
        }
        return almacenamiento.values().stream()
                .anyMatch(s -> s.getNumeroSerie().equalsIgnoreCase(numeroSerie.trim())
                        && s.getEstado() == EstadoSolicitudRMA.PENDIENTE);
    }

    @Override
    public boolean existeAceptadaPorNumeroSerie(String numeroSerie) {
        if (numeroSerie == null || numeroSerie.isBlank()) {
            return false;
        }
        return almacenamiento.values().stream()
                .anyMatch(s -> s.getNumeroSerie().equalsIgnoreCase(numeroSerie.trim())
                        && s.getEstado() == EstadoSolicitudRMA.ACEPTADA);
    }

    @Override
    public List<String> seriesConRmaPendiente(Collection<ItemCompra> items) {
        List<String> series = new ArrayList<>();
        if (items == null) {
            return series;
        }
        for (ItemCompra item : items) {
            if (item == null || item.numeroSerie() == null) {
                continue;
            }
            if (existePendientePorNumeroSerie(item.numeroSerie())) {
                series.add(item.numeroSerie());
            }
        }
        return series;
    }

    @Override
    public List<String> seriesConRmaAceptada(Collection<ItemCompra> items) {
        List<String> series = new ArrayList<>();
        if (items == null) {
            return series;
        }
        for (ItemCompra item : items) {
            if (item == null || item.numeroSerie() == null) {
                continue;
            }
            if (existeAceptadaPorNumeroSerie(item.numeroSerie())) {
                series.add(item.numeroSerie());
            }
        }
        return series;
    }
}