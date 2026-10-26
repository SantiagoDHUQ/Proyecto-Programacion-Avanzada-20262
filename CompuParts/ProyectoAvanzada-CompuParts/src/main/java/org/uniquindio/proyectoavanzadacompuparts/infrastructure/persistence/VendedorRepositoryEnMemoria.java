package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria del puerto de persistencia del agregado Vendedor.
 */
public class VendedorRepositoryEnMemoria implements VendedorRepository {

    private final Map<String, Vendedor> almacenamiento = new HashMap<>();

    @Override
    public Vendedor guardar(Vendedor vendedor) {
        almacenamiento.put(vendedor.getId(), vendedor);
        return vendedor;
    }

    @Override
    public Optional<Vendedor> buscarPorId(String id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }

    @Override
    public List<Vendedor> listarTodos() {
        return new ArrayList<>(almacenamiento.values());
    }

    @Override
    public void eliminar(String id) {
        almacenamiento.remove(id);
    }
}
