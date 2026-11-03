package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.mapper.BuildEntityMapper;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository.BuildJpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Primary
@RequiredArgsConstructor
public class BuildRepositoryJpaAdapter implements BuildRepository {

    private final BuildJpaRepository jpaRepository;
    private final BuildEntityMapper mapper;
    private final ComponenteRepository componenteRepository;

    @Override
    public Build guardar(Build build) {
        var entity = mapper.toEntity(build);
        var saved = jpaRepository.save(entity);
        List<Componente> componentes = cargarComponentes(saved.getComponenteIds());
        return mapper.toDomain(saved, componentes);
    }

    @Override
    public Optional<Build> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(entity -> {
            List<Componente> componentes = cargarComponentes(entity.getComponenteIds());
            return mapper.toDomain(entity, componentes);
        });
    }

    @Override
    public List<Build> listarTodos() {
        return jpaRepository.findAll().stream().map(entity -> {
            List<Componente> componentes = cargarComponentes(entity.getComponenteIds());
            return mapper.toDomain(entity, componentes);
        }).collect(Collectors.toList());
    }

    @Override
    public void eliminar(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Build> buscarActivosPorComponente(String componenteId) {
        return listarTodos().stream()
                .filter(Build::estaActivo)
                .filter(build -> build.contieneComponente(componenteId))
                .toList();
    }

    private List<Componente> cargarComponentes(List<String> componenteIds) {
        List<Componente> componentes = new ArrayList<>();
        if (componenteIds == null) {
            return componentes;
        }
        for (String componenteId : componenteIds) {
            if (componenteId == null) {
                continue;
            }
            Componente componente = componenteRepository.buscarPorId(componenteId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));
            componentes.add(componente);
        }
        return componentes;
    }
}
