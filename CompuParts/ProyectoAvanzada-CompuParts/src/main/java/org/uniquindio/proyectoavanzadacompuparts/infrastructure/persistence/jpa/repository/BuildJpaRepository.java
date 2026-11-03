package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.BuildEntity;

import java.util.UUID;

public interface BuildJpaRepository extends JpaRepository<BuildEntity, UUID> {
}
