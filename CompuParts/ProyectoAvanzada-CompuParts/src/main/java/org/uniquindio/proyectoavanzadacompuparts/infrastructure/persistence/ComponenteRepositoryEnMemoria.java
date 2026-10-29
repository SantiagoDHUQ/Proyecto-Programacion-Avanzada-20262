package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.util.*;

/**
 * Implementación en memoria del puerto de persistencia del agregado Componente.
 */
public class ComponenteRepositoryEnMemoria implements ComponenteRepository {

    private final Map<String, Componente> almacenamiento = new HashMap<>();

    @Override
    public Componente guardar(Componente componente) {
        almacenamiento.put(componente.getId(), componente);
        return componente;
    }

    @Override
    public Optional<Componente> buscarPorId(String id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }

    @Override
    public List<Componente> listarTodos() {
        return new ArrayList<>(almacenamiento.values());
    }

    @Override
    public boolean existeNumeroSerie(String numeroSerie) {
        if (numeroSerie == null || numeroSerie.isBlank()) {
            return false;
        }
        return almacenamiento.values().stream()
                .anyMatch(componente -> componente.existeNumeroSerie(numeroSerie));
    }

    @Override
    public void eliminar(String id) {
        almacenamiento.remove(id);
    }

    @Override
    public Map<String, Componente> buscarPorItems(Collection<ItemCompra> items) {
        Map<String, Componente> componentes = new LinkedHashMap<>();
        if (items == null) {
            return componentes;
        }
        for (ItemCompra item : items) {
            if (item == null || item.componenteId() == null) {
                continue;
            }
            if (componentes.containsKey(item.componenteId())) {
                continue;
            }
            Componente componente = buscarPorId(item.componenteId())
                    .orElseThrow(() -> new org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException("No existe un Componente con ese identificador"));
            componentes.put(componente.getId(), componente);
        }
        return componentes;
    }
}
