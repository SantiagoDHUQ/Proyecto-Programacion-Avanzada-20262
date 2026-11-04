package org.uniquindio.proyectoavanzadacompuparts.infrastructure.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioJpaDataRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}

