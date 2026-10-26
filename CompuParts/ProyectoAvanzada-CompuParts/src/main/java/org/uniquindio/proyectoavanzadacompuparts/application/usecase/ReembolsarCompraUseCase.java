package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.util.LinkedHashMap;
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

    @Transactional
    public Compra ejecutar(String compraId, String compradorId, String motivo) {
        Compra compra = compraRepository.buscarPorId(compraId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una Compra con ese identificador"));

        // El dominio valida la transición
        compra.validarReembolso(compradorId, motivo);

        Map<String, Componente> componentes = new LinkedHashMap<>();

        // Primero valida todas las unidades; solo después descarta y reembolsa.
        for (ItemCompra item : compra.getItems()) {
            Componente componente = componenteRepository.buscarPorId(item.componenteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No existe un Componente con ese identificador"));
            componentes.put(componente.getId(), componente);

            // El dominio valida el estado de la unidad
            componente.unidadPorId(item.unidadId()).validarEstaEnRMA();

            // Verifica que no haya RMA pendiente y que exista RMA aceptada
            if (solicitudRMARepository.existePendientePorNumeroSerie(item.numeroSerie())) {
                throw new ReglaDominioException("La unidad " + item.numeroSerie()
                        + " tiene una SolicitudRMA pendiente");
            }
            if (!solicitudRMARepository.existeAceptadaPorNumeroSerie(item.numeroSerie())) {
                throw new ReglaDominioException("La unidad " + item.numeroSerie()
                        + " no tiene una SolicitudRMA aceptada");
            }
        }

        for (ItemCompra item : compra.getItems()) {
            componentes.get(item.componenteId()).descartarUnidad(item.unidadId());
        }
        componentes.values().forEach(componenteRepository::guardar);

        compra.solicitarReembolso(motivo);
        return compraRepository.guardar(compra);
    }
}
