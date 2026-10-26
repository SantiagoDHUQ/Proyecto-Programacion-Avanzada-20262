package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Caso de uso que cancela una compra pendiente y libera sus unidades.
 *
 * <p>Este es el mecanismo que evita que una compra PENDIENTE bloquee el stock
 * indefinidamente: las unidades reservadas vuelven a DISPONIBLE y el Build
 * puede volver a comprarse.</p>
 */
@Service
@RequiredArgsConstructor
public class CancelarCompraUseCase {

    private final CompraRepository compraRepository;
    private final ComponenteRepository componenteRepository;
    private final ReservaPreventaRepository reservaPreventaRepository;

    @Transactional
    public Compra ejecutar(String compraId) {
        Compra compra = compraRepository.buscarPorId(compraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una Compra con ese identificador"));

        // El dominio valida la transición
        compra.validarCancelar();

        Map<String, Componente> componentes = new LinkedHashMap<>();

        for (ItemCompra item : compra.getItems()) {
            Componente componente = componenteRepository.buscarPorId(item.componenteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));
            componentes.put(componente.getId(), componente);

            // El dominio valida el estado de la unidad
            componente.unidadPorId(item.unidadId()).validarEstaReservada();
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
