package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Caso de uso, del lado del comprador, que reembolsa una compra completada
 * después de que todas sus unidades fueron aceptadas en RMA.
 *
 * <p>El reembolso es definitivo: las unidades no vuelven al stock, pasan a
 * {@code DESCARTADA}. Se valida todo antes de cambiar cualquier estado para no
 * dejar una compra reembolsada a medias.</p>
 */
public class ReembolsarCompraUseCase {

    private final CompraRepository compraRepository;
    private final ComponenteRepository componenteRepository;
    private final SolicitudRMARepository solicitudRMARepository;

    public ReembolsarCompraUseCase(CompraRepository compraRepository,
                                   ComponenteRepository componenteRepository,
                                   SolicitudRMARepository solicitudRMARepository) {
        this.compraRepository = Objects.requireNonNull(compraRepository);
        this.componenteRepository = Objects.requireNonNull(componenteRepository);
        this.solicitudRMARepository = Objects.requireNonNull(solicitudRMARepository);
    }

    public Compra ejecutar(String compraId, String compradorId, String motivo) {
        Compra compra = compraRepository.buscarPorId(compraId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una Compra con ese identificador"));

        if (compra.getEstado() != EstadoCompra.COMPLETADA) {
            throw new ReglaDominioException("Solo se puede reembolsar una compra completada");
        }
        if (compradorId == null || !compra.getCompradorId().equals(compradorId)) {
            throw new ReglaDominioException("Solo el comprador de la compra puede solicitar el reembolso");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("El reembolso exige indicar un motivo");
        }

        Map<String, Componente> componentes = new LinkedHashMap<>();

        // Primero valida todas las unidades; solo después descarta y reembolsa.
        for (ItemCompra item : compra.getItems()) {
            Componente componente = componenteRepository.buscarPorId(item.componenteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No existe un Componente con ese identificador"));
            componentes.put(componente.getId(), componente);

            UnidadInventario unidad = componente.unidadPorId(item.unidadId());
            if (unidad.getEstado() != EstadoUnidad.EN_RMA) {
                throw new ReglaDominioException("La unidad " + item.numeroSerie()
                        + " no está en RMA; primero debe aceptarse su solicitud");
            }
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
