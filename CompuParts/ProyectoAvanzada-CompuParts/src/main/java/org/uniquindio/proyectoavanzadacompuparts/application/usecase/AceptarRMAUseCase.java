package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;

import java.util.Objects;

/**
 * Caso de uso, del lado del vendedor dueño, que acepta una SolicitudRMA.
 *
 * <p>Regla 9: solo el vendedor propietario del componente puede resolver la
 * solicitud. Aceptar no reembolsa ni descarta la unidad: la unidad permanece
 * EN_RMA hasta que se gestione el reembolso o la reparación.</p>
 */
public class AceptarRMAUseCase {

    private final SolicitudRMARepository solicitudRMARepository;
    private final ComponenteRepository componenteRepository;

    public AceptarRMAUseCase(SolicitudRMARepository solicitudRMARepository,
                             ComponenteRepository componenteRepository) {
        this.solicitudRMARepository = Objects.requireNonNull(solicitudRMARepository);
        this.componenteRepository = Objects.requireNonNull(componenteRepository);
    }

    public SolicitudRMA ejecutar(String solicitudId, String vendedorId) {
        SolicitudRMA solicitud = solicitudRMARepository.buscarPorId(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una SolicitudRMA con ese identificador"));

        Componente componente = componenteRepository.buscarPorId(solicitud.getComponenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el Componente de la SolicitudRMA"));

        componente.validarPropietario(vendedorId);

        if (!solicitud.estaPendiente()) {
            throw new ReglaDominioException("Solo se puede aceptar una SolicitudRMA pendiente");
        }

        UnidadInventario unidad = componente.unidadPorNumeroSerie(solicitud.getNumeroSerie());
        if (unidad.getEstado() != EstadoUnidad.EN_RMA) {
            throw new ReglaDominioException("La unidad no está en RMA");
        }

        solicitud.aceptar();
        return solicitudRMARepository.guardar(solicitud);
    }
}
