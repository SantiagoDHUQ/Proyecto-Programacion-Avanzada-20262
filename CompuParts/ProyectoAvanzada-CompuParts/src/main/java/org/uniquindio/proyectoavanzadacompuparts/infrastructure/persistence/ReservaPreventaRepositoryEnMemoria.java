package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.util.ArrayList;
import java.util.Collection;
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

    @Override
    public boolean esDueno(UUID reservaId, String compradorId) {
        ReservaPreventa reserva = almacenamiento.get(reservaId);
        return reserva != null && reserva.getCompradorId().equals(compradorId);
    }

    @Override
    public List<ReservaPreventa> buscarVigentesEnCompra(Collection<ItemCompra> items) {
        List<ReservaPreventa> reservasVigentes = new ArrayList<>();
        if (items == null) {
            return reservasVigentes;
        }
        for (ItemCompra item : items) {
            if (item == null || item.unidadId() == null) {
                continue;
            }
            buscarVigentePorUnidad(item.unidadId())
                    .filter(ReservaPreventa::estaEnCompra)
                    .ifPresent(reservasVigentes::add);
        }
        return reservasVigentes;
    }
}
