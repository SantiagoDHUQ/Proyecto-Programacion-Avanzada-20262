package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.mapper.VendedorEntityMapper;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository.VendedorJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Primary
@RequiredArgsConstructor
public class VendedorRepositoryJpaAdapter implements VendedorRepository {

    private final VendedorJpaRepository jpaRepository;
    private final VendedorEntityMapper mapper;

    @Override
    public Vendedor guardar(Vendedor vendedor) {
        var entity = mapper.toEntity(vendedor);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Vendedor> buscarPorId(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Vendedor> listarTodos() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(String id) {
        jpaRepository.deleteById(id);
    }
}
