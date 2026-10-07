package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.dto.AgregarItemAPedidoRequestDTO;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Pedido;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.PedidoRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.util.Objects;

public class AgregarItemAPedidoUseCase {

    private final PedidoRepository pedidoRepository;

    public AgregarItemAPedidoUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "El repositorio de pedidos no puede ser nulo");
    }

    public Pedido ejecutar(AgregarItemAPedidoRequestDTO request) {
        Pedido pedido = pedidoRepository.buscarPorId(request.pedidoId())
                .orElseThrow(() -> new ReglaDominioException("No existe un Pedido con ese identificador"));
        
        Precio precio = new Precio(request.precioMonto(), request.precioMoneda());
        pedido.agregarItem(request.productoId(), request.cantidad(), precio);
        
        return pedidoRepository.guardar(pedido);
    }
}
