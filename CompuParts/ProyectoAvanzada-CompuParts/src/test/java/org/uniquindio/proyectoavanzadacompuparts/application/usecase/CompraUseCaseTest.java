package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CompraService;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CompraUseCaseTest {

    @Mock
    private BuildRepository buildRepository;

    @Mock
    private ComponenteRepository componenteRepository;

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private ReservaPreventaRepository reservaPreventaRepository;

    @Spy
    private CompraService compraService = new CompraService(Clock.systemUTC());

    @InjectMocks
    private RealizarCompraUseCase realizarCompra;

    @InjectMocks
    private ConfirmarCompraUseCase confirmarCompra;

    @InjectMocks
    private CancelarCompraUseCase cancelarCompra;

    private Build build;
    private List<Componente> componentes;

    @BeforeEach
    void setUp() {
        Componente cpu = componente("cpu", CategoriaComponente.CPU, "AM5", 120);
        Componente board = componente("mb", CategoriaComponente.MOTHERBOARD, "AM5", 80);
        Componente psu = componente("psu", CategoriaComponente.PSU, "AM5", 500);
        this.componentes = List.of(cpu, board, psu);

        Build build = Build.crear();
        this.componentes.forEach(build::agregarComponente);
        build.marcarComoListoParaCompra();
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
    void realizarCompraReservaUnaUnidadDeCadaComponente() {
        // Arrange
        when(buildRepository.buscarPorId(this.build.getId())).thenReturn(Optional.of(this.build));
        when(compraRepository.guardar(any(Compra.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Compra compra = realizarCompra.ejecutar(this.build.getId(), "comprador-1");

        // Assert
        assertEquals(EstadoCompra.PENDIENTE, compra.getEstado());
        assertEquals(3, compra.getItems().size());
        assertEquals(EstadoBuild.LISTO_PARA_COMPRA, this.build.getEstado());
        for (Componente componente : componentes) {
            assertEquals(0L, componente.stockDisponible());
            assertEquals(EstadoUnidad.RESERVADA, componente.getUnidades().getFirst().getEstado());
        }
        verify(compraRepository).guardar(any(Compra.class));
    }

    @Test
    void unaSegundaCompraNoPuedeUsarLasUnidadesReservadas() {
        // Arrange
        when(buildRepository.buscarPorId(this.build.getId())).thenReturn(Optional.of(this.build));
        when(compraRepository.guardar(any(Compra.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        realizarCompra.ejecutar(this.build.getId(), "comprador-1");

        // Assert
        assertThrows(ReglaDominioException.class,
                () -> realizarCompra.ejecutar(this.build.getId(), "comprador-2"));
    }

    @Test
    void cancelarCompraLiberaElStockYElBuildPuedeVolverAComprarse() {
        // Arrange
        when(buildRepository.buscarPorId(this.build.getId())).thenReturn(Optional.of(this.build));
        when(compraRepository.guardar(any(Compra.class))).thenAnswer(i -> i.getArgument(0));

        Compra compra = realizarCompra.ejecutar(this.build.getId(), "comprador-1");

        when(compraRepository.buscarPorId(compra.getId())).thenReturn(Optional.of(compra));
        Map<String, Componente> compMap = componentes.stream().collect(Collectors.toMap(Componente::getId, c -> c));
        when(componenteRepository.buscarPorItems(compra.getItems())).thenReturn(compMap);
        when(reservaPreventaRepository.buscarVigentesEnCompra(compra.getItems())).thenReturn(List.of());

        // Act
        cancelarCompra.ejecutar(compra.getId());

        // Assert
        assertEquals(EstadoCompra.CANCELADA, compra.getEstado());
        for (Componente componente : componentes) {
            assertEquals(1L, componente.stockDisponible());
            assertEquals(EstadoUnidad.DISPONIBLE, componente.getUnidades().getFirst().getEstado());
        }

        Compra segundaCompra = realizarCompra.ejecutar(this.build.getId(), "comprador-2");
        assertEquals(EstadoCompra.PENDIENTE, segundaCompra.getEstado());
    }

    @Test
    void confirmarCompraVendeLasUnidadesYMarcaElBuildComoComprado() {
        // Arrange
        when(buildRepository.buscarPorId(this.build.getId())).thenReturn(Optional.of(this.build));
        when(compraRepository.guardar(any(Compra.class))).thenAnswer(i -> i.getArgument(0));

        Compra compra = realizarCompra.ejecutar(this.build.getId(), "comprador-1");

        when(compraRepository.buscarPorId(compra.getId())).thenReturn(Optional.of(compra));
        when(buildRepository.obtenerPorIdDeCompra(compra)).thenReturn(this.build);
        Map<String, Componente> compMap = componentes.stream().collect(Collectors.toMap(Componente::getId, c -> c));
        when(componenteRepository.buscarPorItems(compra.getItems())).thenReturn(compMap);
        when(reservaPreventaRepository.buscarVigentesEnCompra(compra.getItems())).thenReturn(List.of());

        // Act
        confirmarCompra.ejecutar(compra.getId());

        // Assert
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
        assertEquals(EstadoBuild.COMPRADO, this.build.getEstado());
        for (Componente componente : componentes) {
            assertEquals(EstadoUnidad.VENDIDA, componente.getUnidades().getFirst().getEstado());
        }
    }

    @Test
    void unaCompraCompletadaNoPuedeCancelarseNiLiberarStock() {
        // Arrange
        when(buildRepository.buscarPorId(this.build.getId())).thenReturn(Optional.of(this.build));
        when(compraRepository.guardar(any(Compra.class))).thenAnswer(i -> i.getArgument(0));

        Compra compra = realizarCompra.ejecutar(this.build.getId(), "comprador-1");

        when(compraRepository.buscarPorId(compra.getId())).thenReturn(Optional.of(compra));
        when(buildRepository.obtenerPorIdDeCompra(compra)).thenReturn(this.build);
        Map<String, Componente> compMap = componentes.stream().collect(Collectors.toMap(Componente::getId, c -> c));
        when(componenteRepository.buscarPorItems(compra.getItems())).thenReturn(compMap);
        when(reservaPreventaRepository.buscarVigentesEnCompra(compra.getItems())).thenReturn(List.of());

        confirmarCompra.ejecutar(compra.getId());

        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> cancelarCompra.ejecutar(compra.getId()));
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
        for (Componente componente : componentes) {
            assertEquals(EstadoUnidad.VENDIDA, componente.getUnidades().getFirst().getEstado());
        }
    }
}
