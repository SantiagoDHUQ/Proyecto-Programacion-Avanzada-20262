package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.dto.CrearPedidoRequestDTO;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Pedido;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.PedidoRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Direccion;

import java.util.Objects;

public class CrearPedidoUseCase {

    private final PedidoRepository pedidoRepository;

    public CrearPedidoUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "El repositorio de pedidos no puede ser nulo");
    }

    public Pedido ejecutar(CrearPedidoRequestDTO request) {
        Direccion direccion = new Direccion(
                request.calle(),
                request.ciudad(),
                request.codigoPostal(),
                request.pais()
        );
        Pedido pedido = Pedido.crear(request.clienteId(), direccion);
        return pedidoRepository.guardar(pedido);
    }
}
