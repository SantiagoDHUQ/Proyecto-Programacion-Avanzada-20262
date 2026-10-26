package org.uniquindio.proyectoavanzadacompuparts.application.dto.response;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ResultadoCompatibilidad;

import java.util.List;

/**
 * DTO de salida para los casos de uso de Build.
 * <p>
 * Expone los ids de los componentes (no las entidades) y los motivos del Chequeo de Compatibilidad.
 */
public record BuildResponse(
        String buildId,
        String estado,
        List<String> componentes,
        boolean compatible,
        List<String> motivos) {

    public static BuildResponse from(Build build) {
        return new BuildResponse(
                build.getId().toString(),
                build.getEstado().name(),
                idsDeComponentes(build),
                build.getEstado() == EstadoBuild.LISTO_PARA_COMPRA,
                List.of()
        );
    }

    public static BuildResponse from(Build build, ResultadoCompatibilidad resultado) {
        return new BuildResponse(
                build.getId().toString(),
                build.getEstado().name(),
                idsDeComponentes(build),
                resultado.compatible(),
                resultado.motivos()
        );
    }

    private static List<String> idsDeComponentes(Build build) {
        return build.getComponentes().stream()
                .map(Componente::getId)
                .toList();
    }
}
