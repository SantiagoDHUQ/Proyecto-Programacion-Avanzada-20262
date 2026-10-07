package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.dto.RemoverItemDePedidoRequestDTO;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Pedido;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.PedidoRepository;

import java.util.Objects;

public class RemoverItemDePedidoUseCase {

    private final PedidoRepository pedidoRepository;

    public RemoverItemDePedidoUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "El repositorio de pedidos no puede ser nulo");
    }

    public Pedido ejecutar(RemoverItemDePedidoRequestDTO request) {
        Pedido pedido = pedidoRepository.buscarPorId(request.pedidoId())
                .orElseThrow(() -> new ReglaDominioException("No existe un Pedido con ese identificador"));
        
        pedido.removerItem(request.productoId());
        
        return pedidoRepository.guardar(pedido);
    }
}
