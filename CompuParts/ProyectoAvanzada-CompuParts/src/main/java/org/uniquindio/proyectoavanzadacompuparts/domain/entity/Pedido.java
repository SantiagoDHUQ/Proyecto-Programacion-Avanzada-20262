package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Direccion;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoPedido;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemPedido;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Pedido {

    private final UUID id;
    private final String clienteId;
    private final Direccion direccionEnvio;
    private EstadoPedido estado;
    private final List<ItemPedido> items;
    private Precio total;

    private Pedido(UUID id, String clienteId, Direccion direccionEnvio) {
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.clienteId = Objects.requireNonNull(clienteId, "El clienteId no puede ser nulo");
        this.direccionEnvio = Objects.requireNonNull(direccionEnvio, "La dirección de envío no puede ser nula");
        this.estado = EstadoPedido.CREADO;
        this.items = new ArrayList<>();
        this.total = new Precio(BigDecimal.ZERO, "COP");
    }

    public static Pedido crear(String clienteId, Direccion direccionEnvio) {
        return new Pedido(UUID.randomUUID(), clienteId, direccionEnvio);
    }

    public static Pedido reconstituir(UUID id, String clienteId, Direccion direccionEnvio, EstadoPedido estado, List<ItemPedido> items, Precio total) {
        Pedido pedido = new Pedido(id, clienteId, direccionEnvio);
        pedido.estado = estado;
        pedido.items.addAll(items);
        pedido.total = total;
        return pedido;
    }

    public void agregarItem(String productoId, int cantidad, Precio precioUnitario) {
        if (estado == EstadoPedido.PAGADO || estado == EstadoPedido.EN_PREPARACION || estado == EstadoPedido.ENVIADO) {
            throw new ReglaDominioException("No se pueden agregar ítems a un pedido en estado " + estado);
        }
        if (cantidad <= 0) {
            throw new ReglaDominioException("La cantidad debe ser mayor a cero");
        }

        Optional<ItemPedido> existente = items.stream()
                .filter(i -> i.productoId().equals(productoId))
                .findFirst();

        if (existente.isPresent()) {
            ItemPedido itemActual = existente.get();
            int nuevaCantidad = itemActual.cantidad() + cantidad;
            ItemPedido nuevoItem = new ItemPedido(productoId, nuevaCantidad, precioUnitario);
            items.remove(itemActual);
            items.add(nuevoItem);
        } else {
            items.add(new ItemPedido(productoId, cantidad, precioUnitario));
        }

        recalcularTotal();
    }

    public void removerItem(String productoId) {
        if (estado == EstadoPedido.PAGADO || estado == EstadoPedido.EN_PREPARACION || estado == EstadoPedido.ENVIADO) {
            throw new ReglaDominioException("No se pueden remover ítems de un pedido en estado " + estado);
        }
        
        boolean removido = items.removeIf(i -> i.productoId().equals(productoId));
        if (removido) {
            recalcularTotal();
        }
    }

    /**
     * Confirma el pago del pedido.
     * Solo permite confirmar el pago si el estado actual es CREADO y tiene ítems.
     */
    public void confirmarPago() {
        if (estado != EstadoPedido.CREADO) {
            throw new ReglaDominioException("El pago solo puede confirmarse si el pedido está en estado CREADO");
        }
        if (items.isEmpty()) {
            throw new ReglaDominioException("No se puede confirmar el pago de un pedido sin ítems");
        }
        this.estado = EstadoPedido.PAGADO;
    }

    public void cancelar(String motivo) {
        // TODO: Definir reglas específicas para la cancelación (por ejemplo, si el pedido ya fue enviado o si se permite cancelar en ciertos estados).
        // Por ahora, se permite cancelar sin restricciones adicionales.
        this.estado = EstadoPedido.CANCELADO;
    }

    private void recalcularTotal() {
        if (items.isEmpty()) {
            this.total = new Precio(BigDecimal.ZERO, "COP");
            return;
        }
        String moneda = items.get(0).precioUnitario().moneda();
        BigDecimal suma = items.stream()
                .map(item -> item.subtotal().monto())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.total = new Precio(suma, moneda);
    }

    public UUID getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public Direccion getDireccionEnvio() {
        return direccionEnvio;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public List<ItemPedido> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Precio getTotal() {
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pedido pedido)) return false;
        return id.equals(pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
