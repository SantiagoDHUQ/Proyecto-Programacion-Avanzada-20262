package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CompraService;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.BuildRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.CompraRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.ComponenteRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.ReservaPreventaRepositoryEnMemoria;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DebugCompraTest {

    private BuildRepositoryEnMemoria buildRepository;
    private ComponenteRepositoryEnMemoria componenteRepository;
    private CompraRepositoryEnMemoria compraRepository;
    private ReservaPreventaRepositoryEnMemoria reservaPreventaRepository;
    private CompraService compraService;
    private RealizarCompraUseCase realizarCompra;
    private CancelarCompraUseCase cancelarCompra;
    private Build build;
    private List<Componente> componentes;

    @BeforeEach
    void setUp() {
        buildRepository = new BuildRepositoryEnMemoria();
        componenteRepository = new ComponenteRepositoryEnMemoria();
        compraRepository = new CompraRepositoryEnMemoria();
        reservaPreventaRepository = new ReservaPreventaRepositoryEnMemoria();
        compraService = new CompraService(java.time.Clock.systemUTC());

        this.realizarCompra = new RealizarCompraUseCase(buildRepository, componenteRepository, compraRepository, compraService);
        this.cancelarCompra = new CancelarCompraUseCase(compraRepository, componenteRepository, reservaPreventaRepository, compraService);

        Componente cpu = componente("cpu", CategoriaComponente.CPU, "AM5", 120);
        Componente board = componente("mb", CategoriaComponente.MOTHERBOARD, "AM5", 80);
        Componente psu = componente("psu", CategoriaComponente.PSU, "AM5", 500);
        this.componentes = List.of(cpu, board, psu);
        this.componentes.forEach(componenteRepository::guardar);

        Build build = Build.crear();
        this.componentes.forEach(build::agregarComponente);
        build.marcarComoListoParaCompra();
        buildRepository.guardar(build);
        this.build = build;
    }

    private Componente componente(String id, CategoriaComponente categoria, String socket, int wattaje) {
        Vendedor vendedor = Vendedor.crear("v1", "Distribuidor Oficial", "AUTORIZADO");
        Componente componente = Componente.crear(id, "pieza-" + id, categoria,
                new EspecificacionTecnica(socket, wattaje, "DDR5"),
                new Precio(new BigDecimal("100000"), "COP"), vendedor, 365);
        componente.agregarUnidad("v1", "SER-" + id, false);
        return componente;
    }

    @Test
    void debugCompraEstadoDespuesDeCrear() {
        Compra compra = realizarCompra.ejecutar(buildRepository.buscarPorId(this.build.getId()).orElseThrow().getId(), "comprador-1");
        System.out.println("Estado de la compra despues de crear: " + compra.getEstado());
        assertEquals(EstadoCompra.PENDIENTE, compra.getEstado());
    }

    @Test
    void cancelarCompraDespuesDeCrear() {
        Compra compra = realizarCompra.ejecutar(buildRepository.buscarPorId(this.build.getId()).orElseThrow().getId(), "comprador-1");
        System.out.println("Estado antes de cancelar: " + compra.getEstado());
        cancelarCompra.ejecutar(compra.getId());
        assertEquals(EstadoCompra.CANCELADA, compra.getEstado());
    }
}
