package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso para listar vendedores.
 */
public class ListarVendedoresUseCase {

    private final VendedorRepository vendedorRepository;

    public ListarVendedoresUseCase(VendedorRepository vendedorRepository) {
        this.vendedorRepository = Objects.requireNonNull(vendedorRepository);
    }

    public List<Vendedor> ejecutar() {
        return vendedorRepository.listarTodos();
    }
}
