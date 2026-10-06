package org.uniquindio.proyectoavanzadacompuparts.domain.repository;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;

import java.util.List;
import java.util.Optional;

public interface VendedorRepository {
    Vendedor guardar(Vendedor componente);

    Optional<Vendedor> buscarPorId(String id);

    List<Vendedor> listarTodos();

    void eliminar(String id);
}
