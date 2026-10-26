package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
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

public class CompraUseCaseTest {

    private BuildRepositoryEnMemoria buildRepository;
    private ComponenteRepositoryEnMemoria componenteRepository;
    private CompraRepositoryEnMemoria compraRepository;
    private RealizarCompraUseCase realizarCompra;
    private ConfirmarCompraUseCase confirmarCompra;
    private CancelarCompraUseCase cancelarCompra;
    private Build build;
    private List<Componente> componentes;

    @BeforeEach
    void setUp() {
        buildRepository = new BuildRepositoryEnMemoria();
        componenteRepository = new ComponenteRepositoryEnMemoria();
        compraRepository = new CompraRepositoryEnMemoria();
        ReservaPreventaRepositoryEnMemoria reservaPreventaRepository = new ReservaPreventaRepositoryEnMemoria();

        realizarCompra = new RealizarCompraUseCase(buildRepository, componenteRepository, compraRepository);
        confirmarCompra = new ConfirmarCompraUseCase(compraRepository, componenteRepository, buildRepository, reservaPreventaRepository);
        cancelarCompra = new CancelarCompraUseCase(compraRepository, componenteRepository, reservaPreventaRepository);

        Componente cpu = componente("cpu", CategoriaComponente.CPU, "AM5", 120);
        Componente board = componente("mb", CategoriaComponente.MOTHERBOARD, "AM5", 80);
        Componente psu = componente("psu", CategoriaComponente.PSU, "AM5", 500);
        componentes = List.of(cpu, board, psu);
        componentes.forEach(componenteRepository::guardar);

        build = Build.crear();
        componentes.forEach(build::agregarComponente);
        build.marcarComoListoParaCompra();
        buildRepository.guardar(build);
    }

    private Componente componente(String id, CategoriaComponente categoria, String socket, int wattaje) {
        Vendedor vendedor = Vendedor.crear("v1", "Distribuidor Oficial", "AUTORIZADO");
        Componente componente = Componente.crear(id, "pieza-" + id, categoria,
                new EspecificacionTecnica(socket, wattaje, "DDR5"),
                new Precio(new BigDecimal("100000"), "COP"), vendedor, 365);
        componente.agregarUnidad("SER-" + id);
        return componente;
    }

    @Test
    void realizarCompraReservaUnaUnidadDeCadaComponente() {
        Compra compra = realizarCompra.ejecutar(build.getId(), "comprador-1");

        assertEquals(EstadoCompra.PENDIENTE, compra.getEstado());
        assertEquals(3, compra.getItems().size());
        assertEquals(EstadoBuild.LISTO_PARA_COMPRA, build.getEstado());
        for (Componente componente : componentes) {
            assertEquals(0L, componente.stockDisponible());
            assertEquals(EstadoUnidad.RESERVADA, componente.getUnidades().getFirst().getEstado());
        }
    }

    @Test
    void unaSegundaCompraNoPuedeUsarLasUnidadesReservadas() {
        realizarCompra.ejecutar(build.getId(), "comprador-1");

        assertThrows(ReglaDominioException.class,
                () -> realizarCompra.ejecutar(build.getId(), "comprador-2"));
    }

    @Test
    void cancelarCompraLiberaElStockYElBuildPuedeVolverAComprarse() {
        Compra compra = realizarCompra.ejecutar(build.getId(), "comprador-1");

        cancelarCompra.ejecutar(compra.getId());

        assertEquals(EstadoCompra.CANCELADA, compra.getEstado());
        for (Componente componente : componentes) {
            assertEquals(1L, componente.stockDisponible());
            assertEquals(EstadoUnidad.DISPONIBLE, componente.getUnidades().getFirst().getEstado());
        }

        Compra segundaCompra = realizarCompra.ejecutar(build.getId(), "comprador-2");
        assertEquals(EstadoCompra.PENDIENTE, segundaCompra.getEstado());
    }

    @Test
    void confirmarCompraVendeLasUnidadesYMarcaElBuildComoComprado() {
        Compra compra = realizarCompra.ejecutar(build.getId(), "comprador-1");

        confirmarCompra.ejecutar(compra.getId());

        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
        assertEquals(EstadoBuild.COMPRADO, build.getEstado());
        for (Componente componente : componentes) {
            assertEquals(EstadoUnidad.VENDIDA, componente.getUnidades().getFirst().getEstado());
        }
    }

    @Test
    void unaCompraCompletadaNoPuedeCancelarseNiLiberarStock() {
        Compra compra = realizarCompra.ejecutar(build.getId(), "comprador-1");
        confirmarCompra.ejecutar(compra.getId());

        assertThrows(ReglaDominioException.class, () -> cancelarCompra.ejecutar(compra.getId()));
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
        for (Componente componente : componentes) {
            assertEquals(EstadoUnidad.VENDIDA, componente.getUnidades().getFirst().getEstado());
        }
    }
}
