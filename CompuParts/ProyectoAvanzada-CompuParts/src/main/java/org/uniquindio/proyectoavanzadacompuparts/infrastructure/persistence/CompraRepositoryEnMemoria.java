package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria del puerto de persistencia del agregado Compra.
 */
public class CompraRepositoryEnMemoria implements CompraRepository {

    private final Map<String, Compra> almacenamiento = new HashMap<>();

    @Override
    public Optional<Compra> buscarPorId(String id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }

    @Override
    public Compra guardar(Compra compra) {
        almacenamiento.put(compra.getId(), compra);
        return compra;
    }

    public List<Compra> listarTodas() {
        return new ArrayList<>(almacenamiento.values());
    }
}
