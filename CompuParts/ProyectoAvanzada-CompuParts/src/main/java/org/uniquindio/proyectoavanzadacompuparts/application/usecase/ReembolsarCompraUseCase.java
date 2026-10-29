package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CompraService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Caso de uso, del lado del comprador, que reembolsa una compra completada
 * después de que todas sus unidades fueron aceptadas en RMA.
 *
 * <p>El reembolso es definitivo: las unidades no vuelven al stock, pasan a
 * {@code DESCARTADA}. Se valida todo antes de cambiar cualquier estado para no
 * dejar una compra reembolsada a medias.</p>
 */
@Service
@RequiredArgsConstructor
public class ReembolsarCompraUseCase {

    private final CompraRepository compraRepository;
    private final ComponenteRepository componenteRepository;
    private final SolicitudRMARepository solicitudRMARepository;
    private final CompraService compraService;

    @Transactional
    public Compra ejecutar(String compraId, String compradorId, String motivo) {
        Compra compra = compraRepository.buscarPorId(compraId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una Compra con ese identificador"));

        Map<String, Componente> componentes = componenteRepository.buscarPorItems(compra.getItems());

        List<String> seriesConRmaPendiente = solicitudRMARepository.seriesConRmaPendiente(compra.getItems());
        List<String> seriesConRmaAceptada = solicitudRMARepository.seriesConRmaAceptada(compra.getItems());

        compraService.reembolsar(compra, compradorId, motivo,
                new ArrayList<>(componentes.values()),
                seriesConRmaPendiente, seriesConRmaAceptada);

        componentes.values().forEach(componenteRepository::guardar);

        return compraRepository.guardar(compra);
    }
}
