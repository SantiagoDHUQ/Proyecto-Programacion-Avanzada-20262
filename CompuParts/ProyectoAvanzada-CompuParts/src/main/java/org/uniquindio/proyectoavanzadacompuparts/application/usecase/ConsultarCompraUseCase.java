package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;

import java.util.Objects;

/**
 * Caso de uso para consultar una compra y sus ítems.
 */
public class ConsultarCompraUseCase {

    private final CompraRepository compraRepository;

    public ConsultarCompraUseCase(CompraRepository compraRepository) {
        this.compraRepository = Objects.requireNonNull(compraRepository);
    }

    public Compra ejecutar(String compraId) {
        return compraRepository.buscarPorId(compraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una Compra con ese identificador"));
    }
}
