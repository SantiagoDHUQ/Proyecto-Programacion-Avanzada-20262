package org.uniquindio.proyectoavanzadacompuparts.domain.repository;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;

import java.util.List;
import java.util.Optional;
/**
 * Puerto del agregado Componente para almacenar y consultar piezas del catálogo.
 */
public interface ComponenteRepository {

    Componente guardar(Componente componente);

    Optional<Componente> buscarPorId(String id);

    List<Componente> listarTodos();

    void eliminar(String id);
}