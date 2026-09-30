package org.uniquindio.proyectoavanzadacompuparts.domain.repository;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto del agregado Componente para almacenar y consultar piezas del catálogo.
 */
public interface ComponenteRepository {

    Componente guardar(Componente componente);

    Optional<Componente> buscarPorId(UUID id);

    List<Componente> listarTodos();

    void eliminar(UUID id);
}
