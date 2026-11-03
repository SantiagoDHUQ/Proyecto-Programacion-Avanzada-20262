package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.mapper;

import org.springframework.stereotype.Component;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.SolicitudRMAEntity;

@Component
public class SolicitudRMAEntityMapper {

    public SolicitudRMAEntity toEntity(SolicitudRMA solicitud) {
        if (solicitud == null) {
            return null;
        }
        SolicitudRMAEntity entity = new SolicitudRMAEntity();
        entity.setId(solicitud.getId());
        entity.setComponenteId(solicitud.getComponenteId());
        entity.setCompraId(solicitud.getCompraId());
        entity.setNumeroSerie(solicitud.getNumeroSerie());
        entity.setCompradorId(solicitud.getCompradorId());
        entity.setFechaCompra(solicitud.getFechaCompra());
        entity.setDuracionGarantia(solicitud.getDuracionGarantia());
        entity.setFechaSolicitud(solicitud.getFechaSolicitud());
        entity.setEstado(solicitud.getEstado());
        entity.setMotivoRechazo(solicitud.getMotivoRechazo());
        return entity;
    }

    public SolicitudRMA toDomain(SolicitudRMAEntity entity) {
        if (entity == null) {
            return null;
        }
        return SolicitudRMA.reconstruir(
                entity.getId(),
                entity.getComponenteId(),
                entity.getCompraId(),
                entity.getNumeroSerie(),
                entity.getCompradorId(),
                entity.getFechaCompra(),
                entity.getDuracionGarantia(),
                entity.getFechaSolicitud(),
                entity.getEstado(),
                entity.getMotivoRechazo()
        );
    }
}
