package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.mapper;

import org.springframework.stereotype.Component;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Garantia;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.embeddable.EspecificacionTecnicaEmbeddable;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.embeddable.GarantiaEmbeddable;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.embeddable.PrecioEmbeddable;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.ComponenteEntity;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.UnidadInventarioEntity;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ComponenteEntityMapper {

    public ComponenteEntity toEntity(Componente componente) {
        if (componente == null) {
            return null;
        }
        ComponenteEntity entity = new ComponenteEntity();
        entity.setId(componente.getId());
        entity.setNombre(componente.getNombre());
        entity.setCategoria(componente.getCategoria());
        if (componente.getEspecificacion() != null) {
            entity.setEspecificacion(new EspecificacionTecnicaEmbeddable(
                    componente.getEspecificacion().socket(),
                    componente.getEspecificacion().wattajeRequerido(),
                    componente.getEspecificacion().tipoMemoria()
            ));
        }
        if (componente.getPrecio() != null) {
            entity.setPrecio(new PrecioEmbeddable(componente.getPrecio().monto(), componente.getPrecio().moneda()));
        }
        if (componente.getVendedor() != null) {
            entity.setVendedorId(componente.getVendedor().getId());
        }
        if (componente.getGarantia().isPresent()) {
            Garantia garantia = componente.getGarantia().get();
            entity.setGarantia(new GarantiaEmbeddable(garantia.dias()));
        } else {
            entity.setGarantia(null);
        }
        entity.setEstadoComercial(componente.getEstadoComercial());
        entity.setUnidades(toUnidadEntities(componente.getUnidades()));
        return entity;
    }

    public Componente toDomain(ComponenteEntity entity, Vendedor vendedor) {
        if (entity == null) {
            return null;
        }
        EspecificacionTecnica especificacion = entity.getEspecificacion() != null
                ? new EspecificacionTecnica(entity.getEspecificacion().getSocket(),
                entity.getEspecificacion().getWattajeRequerido(),
                entity.getEspecificacion().getTipoMemoria())
                : new EspecificacionTecnica(null, 0, null);

        Precio precio = entity.getPrecio() != null
                ? new Precio(entity.getPrecio().getMonto(), entity.getPrecio().getMoneda())
                : null;

        Garantia garantia = entity.getGarantia() != null && entity.getGarantia().getDias() != null
                ? new Garantia(entity.getGarantia().getDias())
                : null;

        List<UnidadInventario> unidades = entity.getUnidades().stream()
                .map(u -> UnidadInventario.reconstruir(
                        u.getId(),
                        u.getNumeroSerie(),
                        u.getEstado(),
                        u.getFechaEstimadaLlegada(),
                        u.getReservaPreventaId()
                ))
                .collect(Collectors.toList());

        return Componente.reconstruir(
                entity.getId(),
                entity.getNombre(),
                entity.getCategoria(),
                especificacion,
                precio,
                vendedor,
                garantia,
                unidades,
                entity.getEstadoComercial()
        );
    }

    private List<UnidadInventarioEntity> toUnidadEntities(List<UnidadInventario> unidades) {
        if (unidades == null) {
            return List.of();
        }
        return unidades.stream().map(u -> {
            UnidadInventarioEntity e = new UnidadInventarioEntity();
            e.setId(u.getId());
            e.setNumeroSerie(u.getNumeroSerie());
            e.setEstado(u.getEstado());
            e.setFechaEstimadaLlegada(u.getFechaEstimadaLlegada());
            e.setReservaPreventaId(u.getReservaPreventaId());
            return e;
        }).collect(Collectors.toList());
    }
}
