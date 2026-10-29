package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.SolicitarRMARequest;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.RmaService;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

/**
 * Caso de uso, del lado del comprador, que solicita una RMA para una unidad
 * concreta de una compra completada y dentro de garantía.
 *
 * <p>La solicitud se identifica por el número de serie de la unidad. Al crearse,
 * la unidad pasa a {@code EN_RMA} para que no vuelva a venderse mientras el
 * vendedor dueño resuelve la solicitud.</p>
 */
@Service
@RequiredArgsConstructor
public class SolicitarRMAUseCase {

    private final CompraRepository compraRepository;
    private final ComponenteRepository componenteRepository;
    private final SolicitudRMARepository solicitudRMARepository;
    private final RmaService rmaService;

    @Transactional
    public SolicitudRMA ejecutar(SolicitarRMARequest request) {
        boolean idYaExiste = solicitudRMARepository.buscarPorId(request.id()).isPresent();

        Compra compra = compraRepository.buscarPorId(request.compraId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una Compra con ese identificador"));

        String numeroSerie = request.numeroSerie();
        String componenteId = compraRepository.obtenerComponenteIdPorNumeroSerie(compra, numeroSerie);
        Componente componente = componenteId == null ? null : componenteRepository.buscarPorId(componenteId).orElse(null);
        boolean hayPendiente = numeroSerie == null ? false : solicitudRMARepository.existePendientePorNumeroSerie(numeroSerie);

        SolicitudRMA solicitud = rmaService.solicitar(
                compra, componente, numeroSerie,
                request.compradorId(), request.fechaSolicitud(), hayPendiente, request.id(), idYaExiste
        );

        if (componente != null) {
            componenteRepository.guardar(componente);
        }
        return solicitudRMARepository.guardar(solicitud);
    }
}
