package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

/**
 * Estado comercial del SKU (Componente), independiente de sus unidades físicas.
 *
 * <p>ELIMINADO es un borrado lógico: el componente deja de venderse pero conserva
 * su historial y sus unidades. AGOTADO también lo oculta de la venta, pero es la
 * operación permitida cuando el componente está dentro de un Build activo.</p>
 */
public enum EstadoComercial {
    PUBLICADO,
    AGOTADO,
    ELIMINADO
}
