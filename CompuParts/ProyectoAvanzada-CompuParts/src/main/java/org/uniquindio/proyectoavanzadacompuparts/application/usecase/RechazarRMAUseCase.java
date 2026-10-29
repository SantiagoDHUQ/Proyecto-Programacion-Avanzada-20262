package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.RmaService;

/**
 * Caso de uso, del lado del vendedor dueño, que rechaza una SolicitudRMA.
 *
 * <p>Regla 9: solo el vendedor propietario del componente puede resolver la
 * solicitud. Si la rechaza, la unidad vuelve a VENDIDA y el motivo es
 * obligatorio.</p>
 */
@Service
@RequiredArgsConstructor
public class RechazarRMAUseCase {

    private final SolicitudRMARepository solicitudRMARepository;
    private final ComponenteRepository componenteRepository;
    private final RmaService rmaService;

    @Transactional
    public SolicitudRMA ejecutar(String solicitudId, String vendedorId, String motivo) {
        SolicitudRMA solicitud = solicitudRMARepository.buscarPorId(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una SolicitudRMA con ese identificador"));

        Componente componente = componenteRepository.buscarPorId(solicitud.getComponenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el Componente de la SolicitudRMA"));

        // Una llamada de dominio
        rmaService.rechazar(solicitud, componente, vendedorId, motivo);

        return solicitudRMARepository.guardar(solicitud);
    }
}