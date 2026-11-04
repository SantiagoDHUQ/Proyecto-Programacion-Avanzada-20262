package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity.ComponenteEntity;

public interface ComponenteJpaRepository extends JpaRepository<ComponenteEntity, String> {

    /**
     * Consulta paginada de componentes activos filtrando por nombre y/o categoría.
     * Incluye siempre la condición c.activo = true para respetar el borrado lógico.
     */
    @Query("""
        SELECT c FROM ComponenteEntity c
        WHERE c.activo = true
          AND (:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND (:categoria IS NULL OR c.categoria = :categoria)
    """)
    Page<ComponenteEntity> buscarPorNombreYCategoria(
            @Param("nombre") String nombre,
            @Param("categoria") CategoriaComponente categoria,
            Pageable pageable
    );
}
