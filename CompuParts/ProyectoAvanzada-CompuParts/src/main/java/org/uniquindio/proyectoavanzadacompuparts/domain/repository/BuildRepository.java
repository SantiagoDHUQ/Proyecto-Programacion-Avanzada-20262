package org.uniquindio.proyectoavanzadacompuparts.domain.repository;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto del agregado Build para persistir y consultar ensamblados en memoria.
 */
public interface BuildRepository {

    Build guardar(Build build);

    Optional<Build> buscarPorId(UUID id);

    List<Build> listarTodos();

    void eliminar(UUID id);
}
