package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Pedido;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.PedidoRepository;

import java.util.Objects;
import java.util.UUID;

public class ConfirmarPagoPedidoUseCase {

    private final PedidoRepository pedidoRepository;

    public ConfirmarPagoPedidoUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "El repositorio de pedidos no puede ser nulo");
    }

    public Pedido ejecutar(UUID pedidoId) {
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Pedido con ese identificador"));
        
        pedido.confirmarPago();
        
        return pedidoRepository.guardar(pedido);
    }
}
