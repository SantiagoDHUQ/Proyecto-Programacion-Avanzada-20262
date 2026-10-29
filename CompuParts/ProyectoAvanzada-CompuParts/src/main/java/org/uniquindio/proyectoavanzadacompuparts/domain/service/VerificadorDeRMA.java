package org.uniquindio.proyectoavanzadacompuparts.domain.service;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de dominio para validar reglas de negocio que cruzan agregados
 * relacionados con las solicitudes de RMA (garantía).
 * <p>
 * No depende de Spring: es una clase POJO del dominio. Se configura como bean
 * en {@link org.uniquindio.proyectoavanzadacompuparts.infrastructure.config.DomainServiceConfig}.
 */
public class VerificadorDeRMA {

    private final CompraRepository compraRepository;
    private final ComponenteRepository componenteRepository;
    private final SolicitudRMARepository solicitudRMARepository;

    public VerificadorDeRMA(CompraRepository compraRepository, ComponenteRepository componenteRepository,
                            SolicitudRMARepository solicitudRMARepository) {
        this.compraRepository = compraRepository;
        this.componenteRepository = componenteRepository;
        this.solicitudRMARepository = solicitudRMARepository;
    }

    /**
     * Valida que se pueda solicitar una RMA para una unidad de una compra.
     * Lanza ReglaDominioException si no se cumplen las condiciones.
     * <p>
     * Verifica:
     * - La compra existe y está en estado COMPLETADA
     * - El número de serie pertenece a la compra
     * - El comprador es el dueño de la compra
     * - La unidad está en estado VENDIDA
     * - El componente admite RMA (garantía vigente y vendedor autorizado)
     * - No existe otra SolicitudRMA pendiente para esa unidad
     */
    public void validarSolicitudRMA(String compraId, String numeroSerie, String compradorId, LocalDate fechaSolicitud) {
        Compra compra = compraRepository.buscarPorId(compraId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Compra con ese identificador"));

        // Validaciones de la compra
        compra.validarParaSolicitarRMA(compradorId);
        compra.validarNumeroSeriePertenece(numeroSerie);

        // Buscar el item y componente
        ItemCompra item = compra.getItems().stream()
                .filter(i -> i.numeroSerie().equalsIgnoreCase(numeroSerie.trim()))
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException("El número de serie no pertenece a la compra"));

        Componente componente = componenteRepository.buscarPorId(item.componenteId())
                .orElseThrow(() -> new ReglaDominioException("No existe un Componente con ese identificador"));

        // Verificar que no haya RMA pendiente para esa unidad
        if (solicitudRMARepository.existePendientePorNumeroSerie(numeroSerie)) {
            throw new ReglaDominioException("Ya existe una SolicitudRMA pendiente para esa unidad");
        }

        // Verificar estado de la unidad
        UnidadInventario unidad = componente.unidadPorId(item.unidadId());
        unidad.validarEstaVendida();

        // Verificar garantía
        LocalDate fechaCompra = compra.getFechaCompra().toLocalDate();
        if (!componente.admiteRMA(fechaCompra, fechaSolicitud)) {
            throw new ReglaDominioException("El componente no tiene garantía vigente para RMA");
        }
    }

    /**
     * Valida que el vendedor sea el dueño del componente de la RMA.
     */
    public void validarDuenoComponente(String componenteId, String vendedorId) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Componente con ese identificador"));
        componente.validarPropietario(vendedorId);
    }
}