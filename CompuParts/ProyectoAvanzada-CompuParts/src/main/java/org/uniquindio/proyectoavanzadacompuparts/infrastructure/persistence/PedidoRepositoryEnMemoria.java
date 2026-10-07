package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Pedido;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.PedidoRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PedidoRepositoryEnMemoria implements PedidoRepository {

    private final Map<UUID, Pedido> almacenamiento = new HashMap<>();

    @Override
    public Pedido guardar(Pedido pedido) {
        almacenamiento.put(pedido.getId(), pedido);
        return pedido;
    }

    @Override
    public Optional<Pedido> buscarPorId(UUID id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }
}
