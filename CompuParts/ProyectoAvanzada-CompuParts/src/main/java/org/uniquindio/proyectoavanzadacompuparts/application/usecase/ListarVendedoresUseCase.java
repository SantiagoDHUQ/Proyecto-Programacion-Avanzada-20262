package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;

import java.util.List;
/**
 * Caso de uso para listar vendedores.
 */
@Service
@RequiredArgsConstructor
public class ListarVendedoresUseCase {

    private final VendedorRepository vendedorRepository;

    @Transactional
    public List<Vendedor> ejecutar() {
        return vendedorRepository.listarTodos();
    }
}
