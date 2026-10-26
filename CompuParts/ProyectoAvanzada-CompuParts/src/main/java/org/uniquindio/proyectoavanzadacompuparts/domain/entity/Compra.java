package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import lombok.Getter;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Raíz del agregado Compra.
 *
 * <p>Una compra agrupa unidades físicas concretas (ítems). Cada ítem conserva
 * el número de serie y el precio del momento de la compra.</p>
 *
 * <p>Invariantes que garantiza:</p>
 * <ul>
 *   <li>Toda compra nace en estado PENDIENTE.</li>
 *   <li>Una compra debe tener al menos un ítem y no puede repetir una unidad.</li>
 *   <li>El precio total queda congelado al momento de comprar.</li>
 *   <li>PENDIENTE puede pasar a COMPLETADA o CANCELADA.</li>
 *   <li>COMPLETADA puede pasar a REEMBOLSADA.</li>
 *   <li>CANCELADA y REEMBOLSADA son estados finales.</li>
 * </ul>
 *
 * <p>El agregado Compra no conoce el inventario: los casos de uso son quienes
 * reservan, liberan o venden las unidades al cambiar el estado de la compra.</p>
 */
@Getter
public class Compra {

    private final String id;
    private final UUID buildId;          // puede ser null en una compra directa (p. ej. preventa)
    private final String compradorId;
    private final List<ItemCompra> items;
    private final Precio precioCongelado;
    private final LocalDateTime fechaCompra;
    private EstadoCompra estado;

    private Compra(String id, UUID buildId, String compradorId, List<ItemCompra> items) {
        this.id = id;
        this.buildId = buildId;
        this.compradorId = compradorId;
        this.items = List.copyOf(items);
        this.precioCongelado = calcularTotal(this.items);
        this.fechaCompra = LocalDateTime.now();
        this.estado = EstadoCompra.PENDIENTE;
    }

    public static Compra realizar(String id, UUID buildId, String compradorId, List<ItemCompra> items) {
        exigir(id != null && !id.isBlank(), "El identificador de la compra es obligatorio");
        exigir(compradorId != null && !compradorId.isBlank(), "La compra debe indicar el comprador");
        exigir(items != null && !items.isEmpty(), "La compra debe tener al menos un ítem");
        exigir(items.stream().allMatch(Objects::nonNull), "La compra no puede tener ítems nulos");

        long unidadesDistintas = items.stream()
                .map(ItemCompra::unidadId)
                .distinct()
                .count();
        exigir(unidadesDistintas == items.size(),
                "Una compra no puede incluir dos veces la misma unidad");

        return new Compra(id, buildId, compradorId, items);
    }

    private static Precio calcularTotal(List<ItemCompra> items) {
        return items.stream()
                .map(ItemCompra::precioUnitario)
                .reduce(Precio::sumar)
                .orElseThrow(() -> new ReglaDominioException("La compra debe tener al menos un ítem"));
    }

    // ---------- Ciclo de vida ----------

    public void confirmar() {
        verificarQueNoEsteFinal();
        verificarTransicion(EstadoCompra.COMPLETADA);
        this.estado = EstadoCompra.COMPLETADA;
    }

    /**
     * Cancela una compra pendiente. El caso de uso que la invoque debe liberar
     * las unidades reservadas; el agregado solo conoce su propio estado.
     */
    public void cancelar() {
        verificarQueNoEsteFinal();
        verificarTransicion(EstadoCompra.CANCELADA);
        this.estado = EstadoCompra.CANCELADA;
    }

    public void solicitarReembolso(String motivo) {
        verificarQueNoEsteFinal();
        if (this.estado != EstadoCompra.COMPLETADA) {
            throw new ReglaDominioException("Solo se puede reembolsar una compra completada");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("El reembolso exige indicar un motivo");
        }
        verificarTransicion(EstadoCompra.REEMBOLSADA);
        this.estado = EstadoCompra.REEMBOLSADA;
    }

    // ---------- Verificaciones internas ----------

    private void verificarQueNoEsteFinal() {
        if (this.estado.esFinal()) {
            throw new ReglaDominioException("Una compra cancelada o reembolsada no puede modificarse");
        }
    }

    private void verificarTransicion(EstadoCompra siguiente) {
        if (!this.estado.puedeTransicionarA(siguiente)) {
            throw new ReglaDominioException("No se puede pasar de " + this.estado + " a " + siguiente);
        }
    }

    // ---------- Consultas ----------

    public List<UUID> getUnidadesReservadas() {
        return items.stream().map(ItemCompra::unidadId).toList();
    }

    public EstadoCompra getEstado() {
        return estado;
    }

    public Precio getPrecioCongelado() {
        return precioCongelado;
    }

    public String getId() {
        return id;
    }

    public UUID getBuildId() {
        return buildId;
    }

    public String getCompradorId() {
        return compradorId;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public List<ItemCompra> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Compra otra)) return false;
        return id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
