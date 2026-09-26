package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;

import java.util.*;

/**
 * Implementación en memoria del puerto de persistencia del agregado Build.
 */
public class BuildRepositoryEnMemoria implements BuildRepository {

    private final Map<UUID, Build> almacenamiento = new HashMap<>();

    @Override
    public Build guardar(Build build) {
        almacenamiento.put(build.getId(), build);
        return build;
    }

    @Override
    public Optional<Build> buscarPorId(UUID id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }

    @Override
    public List<Build> listarTodos() {
        return new ArrayList<>(almacenamiento.values());
    }

    @Override
    public void eliminar(UUID id) {
        almacenamiento.remove(id);
    }
}
