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

    /**
     * Indica si el número de serie ya existe en cualquier unidad del catálogo.
     * La unicidad del serial es global, no solo dentro de un Componente.
     */
    boolean existeNumeroSerie(String numeroSerie);

    void eliminar(String id);
}