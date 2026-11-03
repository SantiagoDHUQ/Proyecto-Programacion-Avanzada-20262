package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.mapper.ComponenteEntityMapper;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository.ComponenteJpaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Primary
@RequiredArgsConstructor
public class ComponenteRepositoryJpaAdapter implements ComponenteRepository {

    private final ComponenteJpaRepository jpaRepository;
    private final ComponenteEntityMapper mapper;
    private final VendedorRepository vendedorRepository;

    @Override
    public Componente guardar(Componente componente) {
        var entity = mapper.toEntity(componente);
        var saved = jpaRepository.save(entity);
        Vendedor vendedor = vendedorRepository.buscarPorId(saved.getVendedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Vendedor con ese identificador"));
        return mapper.toDomain(saved, vendedor);
    }

    @Override
    public Optional<Componente> buscarPorId(String id) {
        return jpaRepository.findById(id).map(entity -> {
            Vendedor vendedor = vendedorRepository.buscarPorId(entity.getVendedorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Vendedor con ese identificador"));
            return mapper.toDomain(entity, vendedor);
        });
    }

    @Override
    public List<Componente> listarTodos() {
        return jpaRepository.findAll().stream().map(entity -> {
            Vendedor vendedor = vendedorRepository.buscarPorId(entity.getVendedorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Vendedor con ese identificador"));
            return mapper.toDomain(entity, vendedor);
        }).collect(Collectors.toList());
    }

    @Override
    public boolean existeNumeroSerie(String numeroSerie) {
        if (numeroSerie == null || numeroSerie.isBlank()) {
            return false;
        }
        return jpaRepository.findAll().stream()
                .anyMatch(componente -> componente.getUnidades().stream()
                        .anyMatch(u -> numeroSerie.equalsIgnoreCase(u.getNumeroSerie())));
    }

    @Override
    public void eliminar(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Componente> buscarPorIds(List<String> ids) {
        List<Componente> resultado = new ArrayList<>();
        if (ids == null) {
            return resultado;
        }
        for (String id : ids) {
            if (id == null) {
                continue;
            }
            Componente componente = buscarPorId(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));
            resultado.add(componente);
        }
        return resultado;
    }

    @Override
    public Map<String, Componente> buscarPorItems(Collection<ItemCompra> items) {
        Map<String, Componente> componentes = new LinkedHashMap<>();
        if (items == null) {
            return componentes;
        }
        for (ItemCompra item : items) {
            if (item == null || item.componenteId() == null) {
                continue;
            }
            if (componentes.containsKey(item.componenteId())) {
                continue;
            }
            Componente componente = buscarPorId(item.componenteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));
            componentes.put(componente.getId(), componente);
        }
        return componentes;
    }
}
