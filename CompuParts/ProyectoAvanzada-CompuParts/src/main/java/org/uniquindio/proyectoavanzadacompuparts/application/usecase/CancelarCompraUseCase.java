package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Caso de uso que cancela una compra pendiente y libera sus unidades.
 *
 * <p>Este es el mecanismo que evita que una compra PENDIENTE bloquee el stock
 * indefinidamente: las unidades reservadas vuelven a DISPONIBLE y el Build
 * puede volver a comprarse.</p>
 */
public class CancelarCompraUseCase {

    private final CompraRepository compraRepository;
    private final ComponenteRepository componenteRepository;
    private final ReservaPreventaRepository reservaPreventaRepository;

    public CancelarCompraUseCase(CompraRepository compraRepository,
                                 ComponenteRepository componenteRepository,
                                 ReservaPreventaRepository reservaPreventaRepository) {
        this.compraRepository = Objects.requireNonNull(compraRepository);
        this.componenteRepository = Objects.requireNonNull(componenteRepository);
        this.reservaPreventaRepository = Objects.requireNonNull(reservaPreventaRepository);
    }

    public Compra ejecutar(String compraId) {
        Compra compra = compraRepository.buscarPorId(compraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una Compra con ese identificador"));

        if (compra.getEstado() != EstadoCompra.PENDIENTE) {
            throw new ReglaDominioException("Solo se puede cancelar una compra pendiente");
        }

        Map<String, Componente> componentes = new LinkedHashMap<>();

        for (ItemCompra item : compra.getItems()) {
            Componente componente = componenteRepository.buscarPorId(item.componenteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));
            componentes.put(componente.getId(), componente);

            UnidadInventario unidad = componente.unidadPorId(item.unidadId());
            if (!unidad.estaReservada()) {
                throw new ReglaDominioException("La unidad " + item.numeroSerie()
                        + " no está reservada para esta compra");
            }
        }

        for (ItemCompra item : compra.getItems()) {
            componentes.get(item.componenteId()).liberarReservaCompra(item.unidadId());
        }
        componentes.values().forEach(componenteRepository::guardar);

        for (ItemCompra item : compra.getItems()) {
            reservaPreventaRepository.buscarVigentePorUnidad(item.unidadId())
                    .filter(ReservaPreventa::estaEnCompra)
                    .ifPresent(reserva -> {
                        reserva.volverAActiva();
                        reservaPreventaRepository.guardar(reserva);
                    });
        }

        compra.cancelar();
        return compraRepository.guardar(compra);
    }
}
