package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CompraService;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.PreventaService;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso del comprador: convierte una reserva de preventa, ya recibida, en
 * una compra directa (sin Build).
 *
 * <p>La unidad debe haber llegado y seguir RESERVADA para ese comprador. La
 * reserva pasa a EN_COMPRA y queda enlazada con la compra PENDIENTE; si la compra
 * se cancela, la reserva vuelve a ACTIVA y la unidad sigue apartada.</p>
 */
@Service
@RequiredArgsConstructor
public class ComprarReservaPreventaUseCase {

    private final ComponenteRepository componenteRepository;
    private final ReservaPreventaRepository reservaPreventaRepository;
    private final CompraRepository compraRepository;
    private final PreventaService preventaService;
    private final CompraService compraService;

    @Transactional
    public Compra ejecutar(UUID reservaId, String compradorId) {
        ReservaPreventa reserva = reservaPreventaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una reserva de preventa con ese identificador"));

        // Validación de propiedad via servicio de dominio (cruce agregados)
        preventaService.validarDueno(reservaId, compradorId);

        Componente componente = componenteRepository.buscarPorId(reserva.getComponenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));

        // Una llamada de dominio
        Compra compra = compraService.realizarDeReserva(reserva, componente, compradorId);

        reservaPreventaRepository.guardar(reserva);
        componenteRepository.guardar(componente);
        return compraRepository.guardar(compra);
    }
}
