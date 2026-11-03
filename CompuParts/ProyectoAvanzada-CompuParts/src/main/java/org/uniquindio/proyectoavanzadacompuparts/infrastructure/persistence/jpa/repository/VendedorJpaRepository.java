package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.VendedorEntity;

public interface VendedorJpaRepository extends JpaRepository<VendedorEntity, String> {
}
