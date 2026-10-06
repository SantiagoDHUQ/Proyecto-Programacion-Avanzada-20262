package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Caso de uso que solicita una RMA para un Componente dentro de garantía.
 * Requiere ComponenteRepository para validar la entidad referenciada.
 */
public class SolicitarRMAUseCase {

    private final ComponenteRepository componenteRepository;
    private final VendedorRepository vendedorRepository;

    public SolicitarRMAUseCase(ComponenteRepository componenteRepository, VendedorRepository vendedorRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository, "El repositorio de componentes no puede ser nulo");
        this.vendedorRepository = Objects.requireNonNull(vendedorRepository, "El repositorio de vendedores no puede ser nulo");
    }

    public SolicitudRMA ejecutar(String id, String componenteId, String fechaSolicitud, String vendedorId) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Componente con ese identificador"));
        Vendedor vendedor = vendedorRepository.buscarPorId(vendedorId)
                .orElseThrow(()-> new ReglaDominioException("No existe un vendedor con ese identificador"));
        return SolicitudRMA.crear(id, componente, LocalDate.parse(fechaSolicitud));
    }
}