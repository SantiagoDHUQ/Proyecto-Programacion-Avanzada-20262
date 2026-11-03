package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoSolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.mapper.SolicitudRMAEntityMapper;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository.SolicitudRMAJpaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Primary
@RequiredArgsConstructor
public class SolicitudRMARepositoryJpaAdapter implements SolicitudRMARepository {

    private final SolicitudRMAJpaRepository jpaRepository;
    private final SolicitudRMAEntityMapper mapper;

    @Override
    public SolicitudRMA guardar(SolicitudRMA solicitud) {
        var entity = mapper.toEntity(solicitud);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SolicitudRMA> buscarPorId(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<SolicitudRMA> listarTodos() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existePendientePorNumeroSerie(String numeroSerie) {
        if (numeroSerie == null || numeroSerie.isBlank()) {
            return false;
        }
        return jpaRepository.findAll().stream()
                .anyMatch(s -> s.getNumeroSerie() != null && s.getNumeroSerie().equalsIgnoreCase(numeroSerie.trim())
                        && s.getEstado() == EstadoSolicitudRMA.PENDIENTE);
    }

    @Override
    public boolean existeAceptadaPorNumeroSerie(String numeroSerie) {
        if (numeroSerie == null || numeroSerie.isBlank()) {
            return false;
        }
        return jpaRepository.findAll().stream()
                .anyMatch(s -> s.getNumeroSerie() != null && s.getNumeroSerie().equalsIgnoreCase(numeroSerie.trim())
                        && s.getEstado() == EstadoSolicitudRMA.ACEPTADA);
    }

    @Override
    public List<String> seriesConRmaPendiente(Collection<ItemCompra> items) {
        List<String> series = new ArrayList<>();
        if (items == null) {
            return series;
        }
        for (ItemCompra item : items) {
            if (item == null || item.numeroSerie() == null) {
                continue;
            }
            if (existePendientePorNumeroSerie(item.numeroSerie())) {
                series.add(item.numeroSerie());
            }
        }
        return series;
    }

    @Override
    public List<String> seriesConRmaAceptada(Collection<ItemCompra> items) {
        List<String> series = new ArrayList<>();
        if (items == null) {
            return series;
        }
        for (ItemCompra item : items) {
            if (item == null || item.numeroSerie() == null) {
                continue;
            }
            if (existeAceptadaPorNumeroSerie(item.numeroSerie())) {
                series.add(item.numeroSerie());
            }
        }
        return series;
    }
}
