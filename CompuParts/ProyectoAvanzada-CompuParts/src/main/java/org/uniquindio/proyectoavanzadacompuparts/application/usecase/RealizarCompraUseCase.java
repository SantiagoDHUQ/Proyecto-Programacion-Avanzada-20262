package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CompraService;

import java.util.UUID;

/**
 * Caso de uso que realiza una compra de un Build listo.
 *
 * <p>Reserva una unidad física de cada componente. La compra nace PENDIENTE;
 * las unidades quedan RESERVADAS y no pueden ser usadas por otra compra. Si la
 * compra se cancela, las unidades vuelven a estar DISPONIBLES.</p>
 */
@Service
@RequiredArgsConstructor
public class RealizarCompraUseCase {

    private final BuildRepository buildRepository;
    private final ComponenteRepository componenteRepository;
    private final CompraRepository compraRepository;
    private final CompraService compraService;

    @Transactional
    public Compra ejecutar(UUID buildId, String compradorId) {
        Build build = buildRepository.buscarPorId(buildId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Build con ese identificador"));

        // Una llamada de dominio
        Compra compra = compraService.realizarDeBuild(build, compradorId);

        // Guardar componentes con unidades reservadas
        build.getComponentes().forEach(componenteRepository::guardar);

        return compraRepository.guardar(compra);
    }
}
