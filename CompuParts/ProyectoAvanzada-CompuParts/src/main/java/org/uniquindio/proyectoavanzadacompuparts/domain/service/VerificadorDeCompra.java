package org.uniquindio.proyectoavanzadacompuparts.domain.service;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.UUID;

/**
 * Servicio de dominio para validar reglas de negocio que cruzan agregados
 * relacionados con la compra de componentes.
 * <p>
 * No depende de Spring: es una clase POJO del dominio. Se configura como bean
 * en {@link org.uniquindio.proyectoavanzadacompuparts.infrastructure.config.DomainServiceConfig}.
 */
public class VerificadorDeCompra {

    private final BuildRepository buildRepository;
    private final ComponenteRepository componenteRepository;

    public VerificadorDeCompra(BuildRepository buildRepository, ComponenteRepository componenteRepository) {
        this.buildRepository = buildRepository;
        this.componenteRepository = componenteRepository;
    }

    /**
     * Valida que un Build pueda ser comprado por el comprador indicado.
     * Lanza ReglaDominioException si no se cumplen las condiciones.
     */
    public void validarPuedeComprarBuild(UUID buildId, String compradorId) {
        Build build = buildRepository.buscarPorId(buildId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Build con ese identificador"));

        build.validarListoParaComprar();

        if (!build.todosLosComponentesDisponibles()) {
            throw new ReglaDominioException("El Build tiene componentes sin stock disponible");
        }
    }

    /**
     * Valida que un Componente pueda ser comprado directamente por el comprador indicado.
     * Lanza ReglaDominioException si no se cumplen las condiciones.
     */
    public void validarPuedeComprarComponente(String componenteId, String compradorId) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Componente con ese identificador"));

        if (!componente.estaDisponibleParaVenta()) {
            throw new ReglaDominioException("El componente no está disponible para la venta");
        }
    }
}