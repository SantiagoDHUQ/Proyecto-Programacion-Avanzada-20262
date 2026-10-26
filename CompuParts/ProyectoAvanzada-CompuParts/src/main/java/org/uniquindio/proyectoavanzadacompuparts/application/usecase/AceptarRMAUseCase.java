package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;

/**
 * Caso de uso, del lado del vendedor dueño, que acepta una SolicitudRMA.
 *
 * <p>Regla 9: solo el vendedor propietario del componente puede resolver la
 * solicitud. Aceptar no reembolsa ni descarta la unidad: la unidad permanece
 * EN_RMA hasta que se gestione el reembolso o la reparación.</p>
 */
@Service
@RequiredArgsConstructor
public class AceptarRMAUseCase {

    private final SolicitudRMARepository solicitudRMARepository;
    private final ComponenteRepository componenteRepository;

    @Transactional
    public SolicitudRMA ejecutar(String solicitudId, String vendedorId) {
        SolicitudRMA solicitud = solicitudRMARepository.buscarPorId(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una SolicitudRMA con ese identificador"));

        Componente componente = componenteRepository.buscarPorId(solicitud.getComponenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el Componente de la SolicitudRMA"));

        componente.validarPropietario(vendedorId);

        // El dominio valida la transición
        solicitud.validarAceptar();

        UnidadInventario unidad = componente.unidadPorNumeroSerie(solicitud.getNumeroSerie());
        unidad.validarEstaEnRMA();

        solicitud.aceptar();
        return solicitudRMARepository.guardar(solicitud);
    }
}
