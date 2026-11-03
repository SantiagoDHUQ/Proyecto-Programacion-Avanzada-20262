package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.CompraEntity;

public interface CompraJpaRepository extends JpaRepository<CompraEntity, String> {
}
