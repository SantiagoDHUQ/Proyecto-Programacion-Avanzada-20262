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

    private Compra(String id, UUID buildId, String compradorId, List<ItemCompra> items,
                   Precio precioCongelado, LocalDateTime fechaCompra, EstadoCompra estado) {
        this.id = id;
        this.buildId = buildId;
        this.compradorId = compradorId;
        this.items = items == null ? List.of() : List.copyOf(items);
        this.precioCongelado = precioCongelado;
        this.fechaCompra = fechaCompra;
        this.estado = estado;
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

        List<ItemCompra> itemsCopia = List.copyOf(items);
        Precio precioCongelado = calcularTotal(itemsCopia);
        return new Compra(id, buildId, compradorId, itemsCopia, precioCongelado,
                LocalDateTime.now(), EstadoCompra.PENDIENTE);
    }

    public static Compra reconstruir(String id, UUID buildId, String compradorId,
                                     List<ItemCompra> items, Precio precioCongelado,
                                     LocalDateTime fechaCompra, EstadoCompra estado) {
        exigir(id != null && !id.isBlank(), "El identificador de la compra es obligatorio");
        exigir(compradorId != null && !compradorId.isBlank(), "La compra debe indicar el comprador");
        exigir(items != null && !items.isEmpty(), "La compra debe tener al menos un ítem");
        exigir(precioCongelado != null, "El precio congelado es obligatorio");
        exigir(fechaCompra != null, "La fecha de compra es obligatoria");
        exigir(estado != null, "El estado de la compra es obligatorio");
        return new Compra(id, buildId, compradorId, items, precioCongelado, fechaCompra, estado);
    }

    private static Precio calcularTotal(List<ItemCompra> items) {
        return items.stream()
                .map(ItemCompra::precioUnitario)
                .reduce(Precio::sumar)
                .orElseThrow(() -> new ReglaDominioException("La compra debe tener al menos un ítem"));
    }

    // ---------- Ciclo de vida ----------

    /**
     * Confirma la compra: pasa de PENDIENTE a COMPLETADA.
     * Valida que la compra esté en estado PENDIENTE.
     */
    public void confirmar() {
        validarConfirmar();
        verificarQueNoEsteFinal();
        verificarTransicion(EstadoCompra.COMPLETADA);
        this.estado = EstadoCompra.COMPLETADA;
    }

    /**
     * Valida que la compra pueda ser confirmada.
     * Lanza ReglaDominioException si no se cumple la invariante.
     */
    public void validarConfirmar() {
        exigir(this.estado == EstadoCompra.PENDIENTE, "Solo se puede confirmar una compra pendiente");
    }

    /**
     * Cancela una compra pendiente. El caso de uso que la invoque debe liberar
     * las unidades reservadas; el agregado solo conoce su propio estado.
     */
    public void cancelar() {
        validarCancelar();
        verificarQueNoEsteFinal();
        verificarTransicion(EstadoCompra.CANCELADA);
        this.estado = EstadoCompra.CANCELADA;
    }

    /**
     * Valida que la compra pueda ser cancelada.
     * Lanza ReglaDominioException si no se cumple la invariante.
     */
    public void validarCancelar() {
        exigir(this.estado == EstadoCompra.PENDIENTE, "Solo se puede cancelar una compra pendiente");
    }

    /**
     * Solicita el reembolso de una compra completada.
     * Valida que se proporcione un motivo (la validación de estado y comprador
     * debe hacerse antes llamando a validarReembolso).
     */
    public void solicitarReembolso(String motivo) {
        exigir(motivo != null && !motivo.isBlank(), "El reembolso exige indicar un motivo");
        verificarQueNoEsteFinal();
        verificarTransicion(EstadoCompra.REEMBOLSADA);
        this.estado = EstadoCompra.REEMBOLSADA;
    }

    /**
     * Valida que la compra pueda ser reembolsada.
     * Lanza ReglaDominioException si no se cumple la invariante.
     */
    public void validarReembolso(String compradorId, String motivo) {
        exigir(this.estado == EstadoCompra.COMPLETADA, "Solo se puede reembolsar una compra completada");
        exigir(this.compradorId.equals(compradorId), "Solo el comprador de la compra puede solicitar el reembolso");
        exigir(motivo != null && !motivo.isBlank(), "El reembolso exige indicar un motivo");
    }

    /**
     * Verifica que el número de serie pertenezca a esta compra.
     */
    public void validarNumeroSeriePertenece(String numeroSerie) {
        boolean pertenece = items.stream()
                .anyMatch(item -> item.numeroSerie().equalsIgnoreCase(numeroSerie.trim()));
        exigir(pertenece, "El número de serie no pertenece a la compra");
    }

    /**
     * Valida que la compra pueda ser usada para solicitar una RMA.
     * La compra debe estar COMPLETADA y el comprador debe coincidir.
     */
    public void validarParaSolicitarRMA(String compradorId) {
        exigir(this.estado == EstadoCompra.COMPLETADA, "Solo se puede solicitar RMA de una compra completada");
        exigir(this.compradorId.equals(compradorId), "Solo el comprador de la compra puede solicitar el RMA");
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
