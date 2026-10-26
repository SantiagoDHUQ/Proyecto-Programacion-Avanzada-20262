package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;

import java.util.Objects;

/**
 * Caso de uso para consultar un vendedor.
 */
@Service
@RequiredArgsConstructor
public class ConsultarVendedorUseCase {

    private final VendedorRepository vendedorRepository;

    @Transactional
    public Vendedor ejecutar(String vendedorId) {
        return vendedorRepository.buscarPorId(vendedorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Vendedor con ese identificador"));
    }
}
