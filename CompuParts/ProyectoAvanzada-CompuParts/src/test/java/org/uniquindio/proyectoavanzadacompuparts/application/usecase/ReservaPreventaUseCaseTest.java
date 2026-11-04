package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.PreventaService;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservaPreventaUseCaseTest {

    @Mock
    private ComponenteRepository componenteRepository;

    @Mock
    private ReservaPreventaRepository reservaRepository;

    @Spy
    private PreventaService preventaService = new PreventaService(null, Clock.systemUTC());

    @InjectMocks
    private ReservarPreventaUseCase reservarPreventa;

    @InjectMocks
    private CancelarReservaPreventaUseCase cancelarReservaPreventa;

    @InjectMocks
    private RecibirPreventaUseCase recibirPreventa;

    private Componente componente;
    private UnidadInventario unidadPreventa;

    @BeforeEach
    void setUp() {
        componente = crearComponente("comp-1");
        unidadPreventa = componente.agregarPreventa("v1", LocalDate.now().plusDays(10), LocalDate.now());

        lenient().when(componenteRepository.guardar(any(Componente.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(reservaRepository.guardar(any(ReservaPreventa.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Componente crearComponente(String id) {
        Vendedor vendedor = Vendedor.crear("v1", "Distribuidor Oficial", "AUTORIZADO");
        return Componente.crear(id, "pieza-" + id, CategoriaComponente.GPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"),
                new Precio(new BigDecimal("100000"), "COP"), vendedor, 365);
    }

    @Test
    void elCompradorPuedeReservarUnaUnidadEnPreventa() {
        // Arrange
        when(componenteRepository.buscarPorId(componente.getId())).thenReturn(Optional.of(componente));
        when(reservaRepository.buscarActivaPorUnidad(unidadPreventa.getId())).thenReturn(Optional.empty());

        // Act
        ReservaPreventa reserva = reservarPreventa.ejecutar(
                componente.getId(), unidadPreventa.getId(), "comprador-1");

        // Assert
        assertEquals(EstadoReservaPreventa.ACTIVA, reserva.getEstado());
        assertEquals(EstadoUnidad.PREVENTA_RESERVADA, unidadPreventa.getEstado());
        assertEquals(reserva.getId(), unidadPreventa.getReservaPreventaId());
        verify(reservaRepository).guardar(any(ReservaPreventa.class));
    }

    @Test
    void unaUnidadEnPreventaNoPuedeReservarseDosVeces() {
        // Arrange
        when(componenteRepository.buscarPorId(componente.getId())).thenReturn(Optional.of(componente));
        when(reservaRepository.buscarActivaPorUnidad(unidadPreventa.getId())).thenReturn(Optional.empty());

        reservarPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "comprador-1");
        when(reservaRepository.buscarActivaPorUnidad(unidadPreventa.getId())).thenReturn(Optional.of(mock(ReservaPreventa.class)));

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> reservarPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "comprador-2"));
    }

    @Test
    void cancelarLaReservaAntesDeLaLlegadaDevuelveLaUnidadAPreventa() {
        // Arrange
        when(componenteRepository.buscarPorId(componente.getId())).thenReturn(Optional.of(componente));
        when(reservaRepository.buscarActivaPorUnidad(unidadPreventa.getId())).thenReturn(Optional.empty());

        ReservaPreventa reserva = reservarPreventa.ejecutar(
                componente.getId(), unidadPreventa.getId(), "comprador-1");
        when(reservaRepository.buscarPorId(reserva.getId())).thenReturn(Optional.of(reserva));

        // Act
        cancelarReservaPreventa.ejecutar(reserva.getId(), "comprador-1");

        // Assert
        assertEquals(EstadoReservaPreventa.CANCELADA, reserva.getEstado());
        assertEquals(EstadoUnidad.PREVENTA, unidadPreventa.getEstado());
        assertNull(unidadPreventa.getReservaPreventaId());
        assertEquals(1L, componente.stockPreventa());
    }

    @Test
    void recibirUnaPreventaReservadaLaDejaReservadaParaElComprador() {
        // Arrange
        when(componenteRepository.buscarPorId(componente.getId())).thenReturn(Optional.of(componente));
        when(reservaRepository.buscarActivaPorUnidad(unidadPreventa.getId())).thenReturn(Optional.empty());
        when(componenteRepository.existeNumeroSerie("SER-LLEGADA")).thenReturn(false);

        ReservaPreventa reserva = reservarPreventa.ejecutar(
                componente.getId(), unidadPreventa.getId(), "comprador-1");

        // Act
        recibirPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "v1", "SER-LLEGADA");

        // Assert
        assertEquals(EstadoUnidad.RESERVADA, unidadPreventa.getEstado());
        assertEquals("SER-LLEGADA", unidadPreventa.getNumeroSerie());
        assertTrue(reserva.estaActiva());
        assertEquals(0L, componente.stockDisponible());
    }

    @Test
    void cancelarLaReservaDespuesDeLaLlegadaDevuelveLaUnidadAlStock() {
        // Arrange
        when(componenteRepository.buscarPorId(componente.getId())).thenReturn(Optional.of(componente));
        when(reservaRepository.buscarActivaPorUnidad(unidadPreventa.getId())).thenReturn(Optional.empty());
        when(componenteRepository.existeNumeroSerie("SER-LLEGADA")).thenReturn(false);

        ReservaPreventa reserva = reservarPreventa.ejecutar(
                componente.getId(), unidadPreventa.getId(), "comprador-1");
        recibirPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "v1", "SER-LLEGADA");
        when(reservaRepository.buscarPorId(reserva.getId())).thenReturn(Optional.of(reserva));

        // Act
        cancelarReservaPreventa.ejecutar(reserva.getId(), "comprador-1");

        // Assert
        assertEquals(EstadoReservaPreventa.CANCELADA, reserva.getEstado());
        assertEquals(EstadoUnidad.DISPONIBLE, unidadPreventa.getEstado());
        assertNull(unidadPreventa.getReservaPreventaId());
        assertEquals(1L, componente.stockDisponible());
    }

    @Test
    void recibirUnaPreventaSinReservaLaDejaDisponible() {
        // Arrange
        when(componenteRepository.buscarPorId(componente.getId())).thenReturn(Optional.of(componente));
        when(componenteRepository.existeNumeroSerie("SER-LLEGADA")).thenReturn(false);

        // Act
        recibirPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "v1", "SER-LLEGADA");

        // Assert
        assertEquals(EstadoUnidad.DISPONIBLE, unidadPreventa.getEstado());
        assertEquals(1L, componente.stockDisponible());
    }

    @Test
    void recibirUnaPreventaExigeNumeroDeSerie() {
        // Arrange
        when(componenteRepository.buscarPorId(componente.getId())).thenReturn(Optional.of(componente));

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> recibirPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "v1", " "));
    }

    @Test
    void elNumeroDeSerieEsUnicoEnTodoElCatalogo() {
        // Arrange
        when(componenteRepository.buscarPorId(componente.getId())).thenReturn(Optional.of(componente));
        when(componenteRepository.existeNumeroSerie("SER-1")).thenReturn(true);

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> recibirPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "v1", "SER-1"));
    }

    @Test
    void noSePuedeReservarUnaUnidadQueNoEstaEnPreventa() {
        // Arrange
        Componente disponible = crearComponente("comp-2");
        UnidadInventario unidad = disponible.agregarUnidad("v1", "SER-D", false);
        when(componenteRepository.buscarPorId(disponible.getId())).thenReturn(Optional.of(disponible));

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> reservarPreventa.ejecutar(disponible.getId(), unidad.getId(), "comprador-1"));
    }

    @Test
    void noSePuedeCancelarDosVecesLaMismaReserva() {
        // Arrange
        when(componenteRepository.buscarPorId(componente.getId())).thenReturn(Optional.of(componente));
        when(reservaRepository.buscarActivaPorUnidad(unidadPreventa.getId())).thenReturn(Optional.empty());

        ReservaPreventa reserva = reservarPreventa.ejecutar(
                componente.getId(), unidadPreventa.getId(), "comprador-1");
        when(reservaRepository.buscarPorId(reserva.getId())).thenReturn(Optional.of(reserva));
        cancelarReservaPreventa.ejecutar(reserva.getId(), "comprador-1");

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> cancelarReservaPreventa.ejecutar(reserva.getId(), "comprador-1"));
    }

    @Test
    void soloElCompradorQueReservoPuedeCancelarLaReserva() {
        // Arrange
        when(componenteRepository.buscarPorId(componente.getId())).thenReturn(Optional.of(componente));
        when(reservaRepository.buscarActivaPorUnidad(unidadPreventa.getId())).thenReturn(Optional.empty());

        ReservaPreventa reserva = reservarPreventa.ejecutar(
                componente.getId(), unidadPreventa.getId(), "comprador-1");
        when(reservaRepository.buscarPorId(reserva.getId())).thenReturn(Optional.of(reserva));

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> cancelarReservaPreventa.ejecutar(reserva.getId(), "comprador-2"));
        assertTrue(reserva.estaActiva());
        assertEquals(EstadoUnidad.PREVENTA_RESERVADA, unidadPreventa.getEstado());
    }
}
