package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.mapper;

import org.springframework.stereotype.Component;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.VendedorEntity;

@Component
public class VendedorEntityMapper {

    public VendedorEntity toEntity(Vendedor vendedor) {
        if (vendedor == null) {
            return null;
        }
        VendedorEntity entity = new VendedorEntity();
        entity.setId(vendedor.getId());
        entity.setNombre(vendedor.getNombre());
        entity.setTipo(vendedor.getTipo());
        return entity;
    }

    public Vendedor toDomain(VendedorEntity entity) {
        if (entity == null) {
            return null;
        }
        return Vendedor.reconstruir(entity.getId(), entity.getNombre(), entity.getTipo());
    }
}
