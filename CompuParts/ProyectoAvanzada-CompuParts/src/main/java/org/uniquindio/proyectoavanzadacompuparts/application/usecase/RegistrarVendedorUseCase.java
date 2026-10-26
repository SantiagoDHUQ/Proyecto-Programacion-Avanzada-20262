package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.RegistrarVendedorRequest;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;

import java.util.Objects;

/**
 * Caso de uso que registra un vendedor.
 *
 * <p>Sin autenticación real, este registro es el que permite que las operaciones
 * de vendedor validen la regla 9 comparando el {@code vendedorId} del request con
 * el dueño del componente.</p>
 */
public class RegistrarVendedorUseCase {

    private final VendedorRepository vendedorRepository;

    public RegistrarVendedorUseCase(VendedorRepository vendedorRepository) {
        this.vendedorRepository = Objects.requireNonNull(vendedorRepository);
    }

    public Vendedor ejecutar(RegistrarVendedorRequest request) {
        if (vendedorRepository.buscarPorId(request.id()).isPresent()) {
            throw new ReglaDominioException("Ya existe un Vendedor con ese identificador");
        }

        Vendedor vendedor = Vendedor.crear(request.id(), request.nombre(), request.tipo());
        return vendedorRepository.guardar(vendedor);
    }
}
