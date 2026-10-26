package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Caso de uso que confirma una compra pendiente.
 *
 * <p>Las unidades reservadas pasan a VENDIDAS y, si la compra viene de un Build,
 * el Build queda COMPRADO. Primero valida todas las unidades para no dejar una
 * compra a medio confirmar.</p>
 */
public class ConfirmarCompraUseCase {

    private final CompraRepository compraRepository;
    private final ComponenteRepository componenteRepository;
    private final BuildRepository buildRepository;
    private final ReservaPreventaRepository reservaPreventaRepository;

    public ConfirmarCompraUseCase(CompraRepository compraRepository,
                                  ComponenteRepository componenteRepository,
                                  BuildRepository buildRepository,
                                  ReservaPreventaRepository reservaPreventaRepository) {
        this.compraRepository = Objects.requireNonNull(compraRepository);
        this.componenteRepository = Objects.requireNonNull(componenteRepository);
        this.buildRepository = Objects.requireNonNull(buildRepository);
        this.reservaPreventaRepository = Objects.requireNonNull(reservaPreventaRepository);
    }

    public Compra ejecutar(String compraId) {
        Compra compra = compraRepository.buscarPorId(compraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una Compra con ese identificador"));

        if (compra.getEstado() != EstadoCompra.PENDIENTE) {
            throw new ReglaDominioException("Solo se puede confirmar una compra pendiente");
        }

        Build build = null;
        if (compra.getBuildId() != null) {
            build = buildRepository.buscarPorId(compra.getBuildId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe el Build de la compra"));
            if (build.getEstado() != EstadoBuild.LISTO_PARA_COMPRA) {
                throw new ReglaDominioException("El Build de la compra no está listo para compra");
            }
        }

        Map<String, Componente> componentes = new LinkedHashMap<>();

        // Primero valida todo; solo después cambia estados.
        for (ItemCompra item : compra.getItems()) {
            Componente componente = buscarComponente(item.componenteId());
            componentes.put(componente.getId(), componente);
            UnidadInventario unidad = componente.unidadPorId(item.unidadId());
            if (!unidad.estaReservada()) {
                throw new ReglaDominioException("La unidad " + item.numeroSerie() + " no está reservada para esta compra");
            }
        }

        for (ItemCompra item : compra.getItems()) {
            componentes.get(item.componenteId()).venderUnidad(item.unidadId());
        }
        componentes.values().forEach(componenteRepository::guardar);

        compra.confirmar();

        for (ItemCompra item : compra.getItems()) {
            reservaPreventaRepository.buscarVigentePorUnidad(item.unidadId())
                    .filter(ReservaPreventa::estaEnCompra)
                    .ifPresent(reserva -> {
                        reserva.convertir();
                        reservaPreventaRepository.guardar(reserva);
                    });
        }

        if (build != null) {
            build.marcarComoComprado();
            buildRepository.guardar(build);
        }

        return compraRepository.guardar(compra);
    }

    private Componente buscarComponente(String componenteId) {
        return componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));
    }
}
