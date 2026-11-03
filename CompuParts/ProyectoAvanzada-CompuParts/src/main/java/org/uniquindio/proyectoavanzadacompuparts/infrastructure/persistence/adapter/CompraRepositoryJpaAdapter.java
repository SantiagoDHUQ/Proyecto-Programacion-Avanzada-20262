package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.mapper.CompraEntityMapper;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository.CompraJpaRepository;

import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class CompraRepositoryJpaAdapter implements CompraRepository {

    private final CompraJpaRepository jpaRepository;
    private final CompraEntityMapper mapper;

    @Override
    public Compra guardar(Compra compra) {
        var entity = mapper.toEntity(compra);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Compra> buscarPorId(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
