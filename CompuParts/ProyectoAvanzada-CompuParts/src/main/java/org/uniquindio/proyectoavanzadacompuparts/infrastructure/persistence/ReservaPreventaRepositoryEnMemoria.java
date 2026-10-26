package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoReservaPreventa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación en memoria del puerto de reservas de preventa.
 */
public class ReservaPreventaRepositoryEnMemoria implements ReservaPreventaRepository {

    private final Map<UUID, ReservaPreventa> almacenamiento = new HashMap<>();

    @Override
    public ReservaPreventa guardar(ReservaPreventa reserva) {
        almacenamiento.put(reserva.getId(), reserva);
        return reserva;
    }

    @Override
    public Optional<ReservaPreventa> buscarPorId(UUID id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }

    @Override
    public List<ReservaPreventa> listarTodas() {
        return new ArrayList<>(almacenamiento.values());
    }

    @Override
    public Optional<ReservaPreventa> buscarActivaPorUnidad(UUID unidadId) {
        return almacenamiento.values().stream()
                .filter(reserva -> reserva.getUnidadId().equals(unidadId))
                .filter(reserva -> reserva.getEstado() == EstadoReservaPreventa.ACTIVA)
                .findFirst();
    }

    @Override
    public Optional<ReservaPreventa> buscarVigentePorUnidad(UUID unidadId) {
        return almacenamiento.values().stream()
                .filter(reserva -> reserva.getUnidadId().equals(unidadId))
                .filter(reserva -> reserva.getEstado().estaVigente())
                .findFirst();
    }
}
