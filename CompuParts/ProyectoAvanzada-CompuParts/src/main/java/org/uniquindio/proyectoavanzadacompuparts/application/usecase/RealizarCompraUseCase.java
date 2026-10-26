package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.util.ArrayList;
import java.util.List;
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

    @Transactional
    public Compra ejecutar(UUID buildId, String compradorId) {
        Build build = buildRepository.buscarPorId(buildId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Build con ese identificador"));

        // El dominio valida que el build esté listo para comprar
        build.validarListoParaComprar();

        List<ItemCompra> items = new ArrayList<>();
        List<Componente> componentesReservados = new ArrayList<>();

        for (Componente componente : build.getComponentes()) {
            UnidadInventario unidad = componente.reservarPrimeraUnidadDisponible();
            componentesReservados.add(componente);
            items.add(new ItemCompra(
                    unidad.getId(),
                    componente.getId(),
                    unidad.getNumeroSerie(),
                    componente.getPrecio()
            ));
        }

        componentesReservados.forEach(componenteRepository::guardar);

        Compra compra = Compra.realizar(
                UUID.randomUUID().toString(),
                buildId,
                compradorId,
                items
        );

        return compraRepository.guardar(compra);
    }
}
