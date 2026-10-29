package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.RegistrarComponenteRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.factory.ComponenteFactory;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;

/**
 * Caso de uso que registra un nuevo Componente (SKU) en el catálogo.
 *
 * <p>El vendedor debe existir y ser el dueño del SKU. Las unidades físicas se
 * agregan con {@link AgregarUnidadUseCase} para que cada una tenga su serial.</p>
 */
@Service
@RequiredArgsConstructor
public class RegistrarComponenteUseCase {

    private final ComponenteRepository componenteRepository;
    private final VendedorRepository vendedorRepository;
    private final ComponenteFactory componenteFactory;

    @Transactional
    public Componente ejecutar(RegistrarComponenteRequest request) {
        Vendedor vendedor = vendedorRepository.buscarPorId(request.vendedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Vendedor con ese identificador"));
        boolean idYaRegistrado = componenteRepository.buscarPorId(request.id()).isPresent();

        Componente componente = componenteFactory.registrar(vendedor, request.aDatos(), idYaRegistrado);

        return componenteRepository.guardar(componente);
    }
}
