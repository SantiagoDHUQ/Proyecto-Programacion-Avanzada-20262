package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.mapper;

import org.springframework.stereotype.Component;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.embeddable.PrecioEmbeddable;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.CompraEntity;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.ItemCompraEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CompraEntityMapper {

    public CompraEntity toEntity(Compra compra) {
        CompraEntity entity = new CompraEntity();
        entity.setId(compra.getId());
        entity.setBuildId(compra.getBuildId());
        entity.setCompradorId(compra.getCompradorId());
        entity.setFechaCompra(compra.getFechaCompra());
        entity.setEstado(compra.getEstado());
        if (compra.getPrecioCongelado() != null) {
            entity.setPrecioCongelado(new PrecioEmbeddable(compra.getPrecioCongelado().monto(), compra.getPrecioCongelado().moneda()));
        }
        entity.setItems(toItemEntities(compra.getItems()));
        return entity;
    }

    public Compra toDomain(CompraEntity entity) {
        List<ItemCompra> items = entity.getItems().stream()
                .map(i -> new ItemCompra(
                        i.getUnidadId(),
                        i.getComponenteId(),
                        i.getNumeroSerie(),
                        new Precio(i.getPrecioUnitario().getMonto(), i.getPrecioUnitario().getMoneda())))
                .collect(Collectors.toList());

        Precio precioCongelado = entity.getPrecioCongelado() != null
                ? new Precio(entity.getPrecioCongelado().getMonto(), entity.getPrecioCongelado().getMoneda())
                : null;

        return Compra.reconstruir(
                entity.getId(),
                entity.getBuildId(),
                entity.getCompradorId(),
                items,
                precioCongelado,
                entity.getFechaCompra(),
                entity.getEstado()
        );
    }

    private List<ItemCompraEntity> toItemEntities(List<ItemCompra> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream().map(item -> {
            ItemCompraEntity e = new ItemCompraEntity();
            e.setId(UUID.randomUUID());
            e.setUnidadId(item.unidadId());
            e.setComponenteId(item.componenteId());
            e.setNumeroSerie(item.numeroSerie());
            if (item.precioUnitario() != null) {
                e.setPrecioUnitario(new PrecioEmbeddable(item.precioUnitario().monto(), item.precioUnitario().moneda()));
            }
            return e;
        }).collect(Collectors.toList());
    }
}
