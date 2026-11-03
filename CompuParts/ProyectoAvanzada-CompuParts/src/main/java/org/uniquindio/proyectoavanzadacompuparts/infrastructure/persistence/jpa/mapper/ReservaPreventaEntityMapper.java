package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.mapper;

import org.springframework.stereotype.Component;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.ReservaPreventaEntity;

@Component
public class ReservaPreventaEntityMapper {

    public ReservaPreventaEntity toEntity(ReservaPreventa reserva) {
        if (reserva == null) {
            return null;
        }
        ReservaPreventaEntity entity = new ReservaPreventaEntity();
        entity.setId(reserva.getId());
        entity.setComponenteId(reserva.getComponenteId());
        entity.setUnidadId(reserva.getUnidadId());
        entity.setCompradorId(reserva.getCompradorId());
        entity.setFechaReserva(reserva.getFechaReserva());
        entity.setEstado(reserva.getEstado());
        return entity;
    }

    public ReservaPreventa toDomain(ReservaPreventaEntity entity) {
        if (entity == null) {
            return null;
        }
        return ReservaPreventa.reconstruir(
                entity.getId(),
                entity.getComponenteId(),
                entity.getUnidadId(),
                entity.getCompradorId(),
                entity.getFechaReserva(),
                entity.getEstado()
        );
    }
}
