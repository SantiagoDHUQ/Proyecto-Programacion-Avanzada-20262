package org.uniquindio.proyectoavanzadacompuparts.domain.repository;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de las reservas de preventa.
 */
public interface ReservaPreventaRepository {

    ReservaPreventa guardar(ReservaPreventa reserva);

    Optional<ReservaPreventa> buscarPorId(UUID id);

    List<ReservaPreventa> listarTodas();

    Optional<ReservaPreventa> buscarActivaPorUnidad(UUID unidadId);

    /**
     * Reserva vigente (ACTIVA o EN_COMPRA) de una unidad. Se usa para conectar
     * una compra directa con la promesa de compra que la originó.
     */
    Optional<ReservaPreventa> buscarVigentePorUnidad(UUID unidadId);
}
