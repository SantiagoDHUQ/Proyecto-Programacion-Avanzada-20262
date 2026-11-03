package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.mapper;

import org.springframework.stereotype.Component;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.BuildEntity;

import java.util.ArrayList;
import java.util.List;

@Component
public class BuildEntityMapper {

    public BuildEntity toEntity(Build build) {
        if (build == null) {
            return null;
        }
        BuildEntity entity = new BuildEntity();
        entity.setId(build.getId());
        List<String> ids = new ArrayList<>();
        if (build.getComponentes() != null) {
            for (Componente c : build.getComponentes()) {
                if (c != null) {
                    ids.add(c.getId());
                }
            }
        }
        entity.setComponenteIds(ids);
        entity.setEstado(build.getEstado());
        return entity;
    }

    public Build toDomain(BuildEntity entity, List<Componente> componentes) {
        if (entity == null) {
            return null;
        }
        List<Componente> comps = componentes == null ? new ArrayList<>() : new ArrayList<>(componentes);
        return Build.reconstruir(entity.getId(), comps, entity.getEstado());
    }
}
