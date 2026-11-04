package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CompraService;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReembolsoUseCaseTest {

    private static final String VENDEDOR_ID = "vendedor-1";
    private static final String COMPRADOR_ID = "comprador-1";
    private static final String COMPONENTE_ID = "componente-1";
    private static final String COMPRA_ID = "compra-1";
    private static final String SERIAL = "SER-1";
    private static final int GARANTIA_DIAS = 365;

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private ComponenteRepository componenteRepository;

    @Mock
    private SolicitudRMARepository solicitudRepository;

    @Spy
    private CompraService compraService = new CompraService(Clock.systemUTC());

    @InjectMocks
    private ReembolsarCompraUseCase reembolsarCompra;

    private Componente componente;
    private UnidadInventario unidad;
    private Compra compra;

    @BeforeEach
    void setUp() {
        Vendedor vendedor = Vendedor.crear(VENDEDOR_ID, "Distribuidor Oficial", "AUTORIZADO");
        componente = Componente.crear(
                COMPONENTE_ID,
                "CPU",
                CategoriaComponente.CPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"),
                precio(),
                vendedor,
                GARANTIA_DIAS);
        unidad = componente.agregarUnidad(VENDEDOR_ID, SERIAL, false);
        componente.reservarUnidad(unidad.getId());
        componente.venderUnidad(unidad.getId());

        compra = Compra.realizar(COMPRA_ID, null, COMPRADOR_ID,
                List.of(item(unidad, COMPONENTE_ID, SERIAL)));
        compra.confirmar();
    }

    private Precio precio() {
        return new Precio(new BigDecimal("100000"), "COP");
    }

    private ItemCompra item(UnidadInventario unidad, String componenteId, String numeroSerie) {
        return new ItemCompra(unidad.getId(), componenteId, numeroSerie, precio());
    }

    @Test
    void reembolsarUnaCompraConRMAAceptadaDescartaLaUnidad() {
        // Arrange
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorItems(compra.getItems())).thenReturn(Map.of(COMPONENTE_ID, componente));
        when(solicitudRepository.seriesConRmaPendiente(compra.getItems())).thenReturn(List.of());
        when(solicitudRepository.seriesConRmaAceptada(compra.getItems())).thenReturn(List.of(SERIAL));
        when(compraRepository.guardar(any(Compra.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Compra reembolsada = reembolsarCompra.ejecutar(COMPRA_ID, COMPRADOR_ID, "Falla de fábrica");

        // Assert
        assertEquals(EstadoCompra.REEMBOLSADA, reembolsada.getEstado());
        assertEquals(EstadoUnidad.DESCARTADA, unidad.getEstado());
        verify(compraRepository).guardar(any(Compra.class));
    }

    @Test
    void noSePuedeReembolsarUnaCompraSinRMAAceptada() {
        // Arrange
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorItems(compra.getItems())).thenReturn(Map.of(COMPONENTE_ID, componente));
        when(solicitudRepository.seriesConRmaPendiente(compra.getItems())).thenReturn(List.of());
        when(solicitudRepository.seriesConRmaAceptada(compra.getItems())).thenReturn(List.of());

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar(COMPRA_ID, COMPRADOR_ID, "Falla de fábrica"));
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
        assertEquals(EstadoUnidad.VENDIDA, unidad.getEstado());
    }

    @Test
    void noSePuedeReembolsarUnaCompraConRMAPendiente() {
        // Arrange
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorItems(compra.getItems())).thenReturn(Map.of(COMPONENTE_ID, componente));
        when(solicitudRepository.seriesConRmaPendiente(compra.getItems())).thenReturn(List.of(SERIAL));
        when(solicitudRepository.seriesConRmaAceptada(compra.getItems())).thenReturn(List.of());

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar(COMPRA_ID, COMPRADOR_ID, "Falla de fábrica"));
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
    }

    @Test
    void soloElCompradorDeLaCompraPuedeReembolsarla() {
        // Arrange
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorItems(compra.getItems())).thenReturn(Map.of(COMPONENTE_ID, componente));
        when(solicitudRepository.seriesConRmaPendiente(compra.getItems())).thenReturn(List.of());
        when(solicitudRepository.seriesConRmaAceptada(compra.getItems())).thenReturn(List.of(SERIAL));

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar(COMPRA_ID, "otro-comprador", "Falla de fábrica"));
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
    }

    @Test
    void noSePuedeReembolsarUnaCompraPendiente() {
        // Arrange
        Compra pendiente = Compra.realizar("compra-pendiente", null, COMPRADOR_ID,
                List.of(item(unidad, COMPONENTE_ID, SERIAL)));
        when(compraRepository.buscarPorId("compra-pendiente")).thenReturn(Optional.of(pendiente));
        when(componenteRepository.buscarPorItems(pendiente.getItems())).thenReturn(Map.of(COMPONENTE_ID, componente));
        when(solicitudRepository.seriesConRmaPendiente(pendiente.getItems())).thenReturn(List.of());
        when(solicitudRepository.seriesConRmaAceptada(pendiente.getItems())).thenReturn(List.of(SERIAL));

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar("compra-pendiente", COMPRADOR_ID, "Falla de fábrica"));
        assertEquals(EstadoCompra.PENDIENTE, pendiente.getEstado());
    }

    @Test
    void elReembolsoExigeMotivoYNoDescartaLaUnidad() {
        // Arrange
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorItems(compra.getItems())).thenReturn(Map.of(COMPONENTE_ID, componente));
        when(solicitudRepository.seriesConRmaPendiente(compra.getItems())).thenReturn(List.of());
        when(solicitudRepository.seriesConRmaAceptada(compra.getItems())).thenReturn(List.of(SERIAL));

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar(COMPRA_ID, COMPRADOR_ID, "   "));
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
    }

    @Test
    void unaCompraInexistenteNoSePuedeReembolsar() {
        // Arrange
        when(compraRepository.buscarPorId("compra-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class,
                () -> reembolsarCompra.ejecutar("compra-inexistente", COMPRADOR_ID, "Falla de fábrica"));
    }

    @Test
    void siUnaUnidadNoTieneRMAAceptadaNoSeDescartaNinguna() {
        // Arrange
        UnidadInventario segundaUnidad = componente.agregarUnidad(VENDEDOR_ID, "SER-2", false);
        componente.reservarUnidad(segundaUnidad.getId());
        componente.venderUnidad(segundaUnidad.getId());

        Compra compraDosUnidades = Compra.realizar("compra-2", null, COMPRADOR_ID,
                List.of(item(unidad, COMPONENTE_ID, SERIAL),
                        item(segundaUnidad, COMPONENTE_ID, "SER-2")));
        compraDosUnidades.confirmar();

        when(compraRepository.buscarPorId("compra-2")).thenReturn(Optional.of(compraDosUnidades));
        when(componenteRepository.buscarPorItems(compraDosUnidades.getItems())).thenReturn(Map.of(COMPONENTE_ID, componente));
        when(solicitudRepository.seriesConRmaPendiente(compraDosUnidades.getItems())).thenReturn(List.of());
        when(solicitudRepository.seriesConRmaAceptada(compraDosUnidades.getItems())).thenReturn(List.of(SERIAL));

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar("compra-2", COMPRADOR_ID, "Falla de fábrica"));
        assertEquals(EstadoCompra.COMPLETADA, compraDosUnidades.getEstado());
        assertEquals(EstadoUnidad.VENDIDA, segundaUnidad.getEstado());
    }

    @Test
    void unSegundoReembolsoNoCambiaUnaCompraYaReembolsada() {
        // Arrange
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorItems(compra.getItems())).thenReturn(Map.of(COMPONENTE_ID, componente));
        when(solicitudRepository.seriesConRmaPendiente(compra.getItems())).thenReturn(List.of());
        when(solicitudRepository.seriesConRmaAceptada(compra.getItems())).thenReturn(List.of(SERIAL));
        when(compraRepository.guardar(any(Compra.class))).thenAnswer(i -> i.getArgument(0));

        reembolsarCompra.ejecutar(COMPRA_ID, COMPRADOR_ID, "Falla de fábrica");

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar(COMPRA_ID, COMPRADOR_ID, "Otra vez"));
        assertEquals(EstadoCompra.REEMBOLSADA, compra.getEstado());
        assertEquals(EstadoUnidad.DESCARTADA, unidad.getEstado());
    }
}
