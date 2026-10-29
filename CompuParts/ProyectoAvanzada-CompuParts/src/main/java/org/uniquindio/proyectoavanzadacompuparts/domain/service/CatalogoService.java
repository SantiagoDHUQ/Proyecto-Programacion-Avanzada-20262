package org.uniquindio.proyectoavanzadacompuparts.domain.service;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;

import java.util.Collection;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/** Procesos que cruzan agregados (Componente y Build). Puro: sin repositorios ni frameworks. */
public final class CatalogoService {

    /**
     * Regla 9 (el vendedor debe ser el dueño) y regla 6 (no se elimina un Componente que forma
     * parte de un Build activo; solo puede marcarse como agotado). Qué es "activo" lo decide Build.estaActivo().
     */
    public void eliminarComponente(Componente componente, String vendedorId, Collection<Build> builds) {
        exigir(componente != null, "El componente es obligatorio");
        exigir(builds != null, "La lista de Builds es obligatoria");
        componente.validarPropietario(vendedorId);

        boolean enUnBuildActivo = builds.stream()
                .anyMatch(build -> build.estaActivo() && build.contieneComponente(componente.getId()));
        exigir(!enUnBuildActivo,
                "El componente pertenece a un Build activo; solo puede marcarse como agotado");

        componente.eliminar();
    }
}
