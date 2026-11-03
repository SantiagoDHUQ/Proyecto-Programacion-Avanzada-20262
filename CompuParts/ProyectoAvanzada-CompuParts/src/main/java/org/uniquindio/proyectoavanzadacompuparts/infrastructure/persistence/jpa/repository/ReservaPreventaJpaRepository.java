package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.ReservaPreventaEntity;

import java.util.UUID;

public interface ReservaPreventaJpaRepository extends JpaRepository<ReservaPreventaEntity, UUID> {
}
