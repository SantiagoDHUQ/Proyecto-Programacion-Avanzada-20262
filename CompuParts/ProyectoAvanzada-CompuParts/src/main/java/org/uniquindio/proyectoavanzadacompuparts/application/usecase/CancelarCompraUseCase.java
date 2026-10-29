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
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CompraService;

import java.util.ArrayList;
import java.util.List;
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
    private final CompraService compraService;

    @Transactional
    public Compra ejecutar(String compraId) {
        Compra compra = compraRepository.buscarPorId(compraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una Compra con ese identificador"));

        Map<String, Componente> componentes = componenteRepository.buscarPorItems(compra.getItems());
        List<ReservaPreventa> reservasVigentes = reservaPreventaRepository.buscarVigentesEnCompra(compra.getItems());

        compraService.cancelar(compra, new ArrayList<>(componentes.values()), reservasVigentes);

        componentes.values().forEach(componenteRepository::guardar);
        reservasVigentes.forEach(reservaPreventaRepository::guardar);

        return compraRepository.guardar(compra);
    }
}
