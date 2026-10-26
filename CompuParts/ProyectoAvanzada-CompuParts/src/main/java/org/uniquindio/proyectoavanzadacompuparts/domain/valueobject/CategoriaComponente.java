package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Categoría del Componente según el tipo de hardware que representa.
 */
public enum CategoriaComponente {
    GPU,
    CPU,
    MOTHERBOARD,
    RAM,
    PSU,
    STORAGE,
    CASE;

    /**
     * Convierte la representación textual de una categoría en el valor del
     * dominio. La conversión es una regla del dominio, no de la capa de
     * aplicación: por eso vive aquí y no en un caso de uso.
     */
    public static CategoriaComponente desde(String categoria) {
        exigir(categoria != null && !categoria.isBlank(), "La categoría de componente es obligatoria");
        try {
            return valueOf(categoria.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ReglaDominioException("Categoría de componente inválida: " + categoria);
        }
    }
}