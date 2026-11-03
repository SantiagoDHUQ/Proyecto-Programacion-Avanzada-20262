package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.SolicitudRMAEntity;

public interface SolicitudRMAJpaRepository extends JpaRepository<SolicitudRMAEntity, String> {
}
