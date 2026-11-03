package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.ComponenteEntity;

public interface ComponenteJpaRepository extends JpaRepository<ComponenteEntity, String> {
}
