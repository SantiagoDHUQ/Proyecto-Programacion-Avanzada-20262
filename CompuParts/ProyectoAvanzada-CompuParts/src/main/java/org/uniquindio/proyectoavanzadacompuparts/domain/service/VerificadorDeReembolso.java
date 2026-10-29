package org.uniquindio.proyectoavanzadacompuparts.domain.service;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.util.List;

/**
 * Servicio de dominio para validar reglas de negocio que cruzan agregados
 * relacionados con el reembolso de compras.
 * <p>
 * No depende de Spring: es una clase POJO del dominio. Se configura como bean
 * en {@link org.uniquindio.proyectoavanzadacompuparts.infrastructure.config.DomainServiceConfig}.
 */
public class VerificadorDeReembolso {

    private final CompraRepository compraRepository;
    private final SolicitudRMARepository solicitudRMARepository;

    public VerificadorDeReembolso(CompraRepository compraRepository, SolicitudRMARepository solicitudRMARepository) {
        this.compraRepository = compraRepository;
        this.solicitudRMARepository = solicitudRMARepository;
    }

    /**
     * Valida que una compra pueda ser reembolsada.
     * Lanza ReglaDominioException si no se cumplen las condiciones.
     * <p>
     * Verifica:
     * - La compra existe y está en estado COMPLETADA
     * - El comprador es el dueño de la compra
     * - Se proporciona un motivo
     * - Todas las unidades de la compra tienen una SolicitudRMA ACEPTADA
     * - Ninguna unidad tiene una SolicitudRMA PENDIENTE
     */
    public void validarReembolso(String compraId, String compradorId, String motivo) {
        Compra compra = compraRepository.buscarPorId(compraId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Compra con ese identificador"));

        // Validaciones de la propia compra
        compra.validarReembolso(compradorId, motivo);

        // Validar que todas las unidades tengan RMA aceptada
        for (ItemCompra item : compra.getItems()) {
            String numeroSerie = item.numeroSerie();

            if (solicitudRMARepository.existePendientePorNumeroSerie(numeroSerie)) {
                throw new ReglaDominioException("La unidad " + numeroSerie + " tiene una SolicitudRMA pendiente");
            }
            if (!solicitudRMARepository.existeAceptadaPorNumeroSerie(numeroSerie)) {
                throw new ReglaDominioException("La unidad " + numeroSerie + " no tiene una SolicitudRMA aceptada");
            }
        }
    }
}