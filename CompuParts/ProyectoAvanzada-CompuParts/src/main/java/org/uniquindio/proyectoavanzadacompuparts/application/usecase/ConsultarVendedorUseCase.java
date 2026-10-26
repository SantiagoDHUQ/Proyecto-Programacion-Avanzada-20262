package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;

import java.util.Objects;

/**
 * Caso de uso para consultar un vendedor.
 */
public class ConsultarVendedorUseCase {

    private final VendedorRepository vendedorRepository;

    public ConsultarVendedorUseCase(VendedorRepository vendedorRepository) {
        this.vendedorRepository = Objects.requireNonNull(vendedorRepository);
    }

    public Vendedor ejecutar(String vendedorId) {
        return vendedorRepository.buscarPorId(vendedorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Vendedor con ese identificador"));
    }
}
