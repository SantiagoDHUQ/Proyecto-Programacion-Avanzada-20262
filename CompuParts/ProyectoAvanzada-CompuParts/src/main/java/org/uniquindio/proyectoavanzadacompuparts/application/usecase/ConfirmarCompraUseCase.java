package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CompraService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Caso de uso que confirma una compra pendiente.
 *
 * <p>Las unidades reservadas pasan a VENDIDAS y, si la compra viene de un Build,
 * el Build queda COMPRADO. Primero valida todas las unidades para no dejar una
 * compra a medio confirmar.</p>
 */
@Service
@RequiredArgsConstructor
public class ConfirmarCompraUseCase {

    private final CompraRepository compraRepository;
    private final ComponenteRepository componenteRepository;
    private final BuildRepository buildRepository;
    private final ReservaPreventaRepository reservaPreventaRepository;
    private final CompraService compraService;

    @Transactional
    public Compra ejecutar(String compraId) {
        Compra compra = compraRepository.buscarPorId(compraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una Compra con ese identificador"));

        Build build = buildRepository.obtenerPorIdDeCompra(compra);
        Map<String, Componente> componentes = componenteRepository.buscarPorItems(compra.getItems());
        List<ReservaPreventa> reservasVigentes = reservaPreventaRepository.buscarVigentesEnCompra(compra.getItems());

        compraService.confirmar(compra, build, new ArrayList<>(componentes.values()), reservasVigentes);

        componentes.values().forEach(componenteRepository::guardar);
        reservasVigentes.forEach(reservaPreventaRepository::guardar);
        if (build != null) {
            buildRepository.guardar(build);
        }

        return compraRepository.guardar(compra);
    }
}
