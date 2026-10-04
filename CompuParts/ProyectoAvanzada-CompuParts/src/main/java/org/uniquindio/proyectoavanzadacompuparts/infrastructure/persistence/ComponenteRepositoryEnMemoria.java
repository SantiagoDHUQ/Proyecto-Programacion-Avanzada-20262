package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

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
    public void eliminar(String id) {
        almacenamiento.remove(id);
    }
}
