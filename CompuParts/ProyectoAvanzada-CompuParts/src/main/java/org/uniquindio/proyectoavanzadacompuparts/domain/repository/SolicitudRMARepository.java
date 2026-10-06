package org.uniquindio.proyectoavanzadacompuparts.domain.repository;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;

import java.util.List;
import java.util.Optional;

/**
 * Puerto del agregado SolicitudRMA para persistir y consultar solicitudes de garantía.
 */
public interface SolicitudRMARepository {

    SolicitudRMA guardar(SolicitudRMA solicitud);

    Optional<SolicitudRMA> buscarPorId(String id);

    List<SolicitudRMA> listarTodos();

    /** Apoya la regla entre agregados: no puede haber dos solicitudes PENDIENTES para el mismo componente. */
    boolean existePendientePorComponente(String componenteId);
}