package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.RegistrarVendedorRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.factory.VendedorFactory;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;

/**
 * Caso de uso que registra un vendedor.
 *
 * <p>Sin autenticación real, este registro es el que permite que las operaciones
 * de vendedor validen la regla 9 comparando el {@code vendedorId} del request con
 * el dueño del componente.</p>
 */
@Service
@RequiredArgsConstructor
public class RegistrarVendedorUseCase {

    private final VendedorRepository vendedorRepository;
    private final VendedorFactory vendedorFactory;

    @Transactional
    public Vendedor ejecutar(RegistrarVendedorRequest request) {
        boolean idYaRegistrado = vendedorRepository.buscarPorId(request.id()).isPresent();

        Vendedor vendedor = vendedorFactory.registrar(request.id(), request.nombre(), request.tipo(), idYaRegistrado);

        return vendedorRepository.guardar(vendedor);
    }
}
