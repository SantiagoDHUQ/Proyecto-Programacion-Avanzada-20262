package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.mapper.ReservaPreventaEntityMapper;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository.ReservaPreventaJpaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Primary
@RequiredArgsConstructor
public class ReservaPreventaRepositoryJpaAdapter implements ReservaPreventaRepository {

    private final ReservaPreventaJpaRepository jpaRepository;
    private final ReservaPreventaEntityMapper mapper;

    @Override
    public ReservaPreventa guardar(ReservaPreventa reserva) {
        var entity = mapper.toEntity(reserva);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ReservaPreventa> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ReservaPreventa> listarTodas() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReservaPreventa> buscarActivaPorUnidad(UUID unidadId) {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .filter(reserva -> reserva.getUnidadId().equals(unidadId))
                .filter(reserva -> reserva.getEstado() == EstadoReservaPreventa.ACTIVA)
                .findFirst();
    }

    @Override
    public Optional<ReservaPreventa> buscarVigentePorUnidad(UUID unidadId) {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .filter(reserva -> reserva.getUnidadId().equals(unidadId))
                .filter(reserva -> reserva.getEstado().estaVigente())
                .findFirst();
    }

    @Override
    public boolean esDueno(UUID reservaId, String compradorId) {
        return buscarPorId(reservaId)
                .map(reserva -> reserva.getCompradorId().equals(compradorId))
                .orElse(false);
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
