package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso del comprador: convierte una reserva de preventa, ya recibida, en
 * una compra directa (sin Build).
 *
 * <p>La unidad debe haber llegado y seguir RESERVADA para ese comprador. La
 * reserva pasa a EN_COMPRA y queda enlazada con la compra PENDIENTE; si la compra
 * se cancela, la reserva vuelve a ACTIVA y la unidad sigue apartada.</p>
 */
public class ComprarReservaPreventaUseCase {

    private final ComponenteRepository componenteRepository;
    private final ReservaPreventaRepository reservaPreventaRepository;
    private final CompraRepository compraRepository;

    public ComprarReservaPreventaUseCase(ComponenteRepository componenteRepository,
                                         ReservaPreventaRepository reservaPreventaRepository,
                                         CompraRepository compraRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository);
        this.reservaPreventaRepository = Objects.requireNonNull(reservaPreventaRepository);
        this.compraRepository = Objects.requireNonNull(compraRepository);
    }

    public Compra ejecutar(UUID reservaId, String compradorId) {
        ReservaPreventa reserva = reservaPreventaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una reserva de preventa con ese identificador"));

        if (!reserva.getCompradorId().equals(compradorId)) {
            throw new ReglaDominioException("Solo el comprador que reservó puede comprar esta unidad");
        }
        if (reserva.estaEnCompra()) {
            throw new ReglaDominioException("La reserva ya tiene una compra pendiente");
        }
        if (!reserva.estaActiva()) {
            throw new ReglaDominioException("La reserva de preventa no está activa");
        }

        Componente componente = componenteRepository.buscarPorId(reserva.getComponenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));
        UnidadInventario unidad = componente.unidadPorId(reserva.getUnidadId());

        if (unidad.getEstado() != EstadoUnidad.RESERVADA) {
            throw new ReglaDominioException("La unidad aún no ha llegado o ya no está reservada para el comprador");
        }
        if (!reserva.getId().equals(unidad.getReservaPreventaId())) {
            throw new ReglaDominioException("La unidad no está reservada por esta preventa");
        }

        ItemCompra item = new ItemCompra(
                unidad.getId(),
                componente.getId(),
                unidad.getNumeroSerie(),
                componente.getPrecio()
        );

        reserva.iniciarCompra();
        Compra compra = Compra.realizar(
                UUID.randomUUID().toString(),
                null,
                compradorId,
                List.of(item)
        );

        reservaPreventaRepository.guardar(reserva);
        return compraRepository.guardar(compra);
    }
}
