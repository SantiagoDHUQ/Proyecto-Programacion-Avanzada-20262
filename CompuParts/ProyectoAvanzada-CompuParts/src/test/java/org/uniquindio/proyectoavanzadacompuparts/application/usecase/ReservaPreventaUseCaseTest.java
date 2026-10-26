package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.ComponenteRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.ReservaPreventaRepositoryEnMemoria;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ReservaPreventaUseCaseTest {

    private ComponenteRepositoryEnMemoria componenteRepository;
    private ReservaPreventaRepositoryEnMemoria reservaRepository;
    private ReservarPreventaUseCase reservarPreventa;
    private CancelarReservaPreventaUseCase cancelarReservaPreventa;
    private RecibirPreventaUseCase recibirPreventa;
    private Componente componente;
    private UnidadInventario unidadPreventa;

    @BeforeEach
    void setUp() {
        componenteRepository = new ComponenteRepositoryEnMemoria();
        reservaRepository = new ReservaPreventaRepositoryEnMemoria();
        reservarPreventa = new ReservarPreventaUseCase(componenteRepository, reservaRepository);
        cancelarReservaPreventa = new CancelarReservaPreventaUseCase(componenteRepository, reservaRepository);
        recibirPreventa = new RecibirPreventaUseCase(componenteRepository);

        componente = crearComponente("comp-1");
        unidadPreventa = componente.agregarPreventa(LocalDate.now().plusDays(10));
        componenteRepository.guardar(componente);
    }

    private Componente crearComponente(String id) {
        Vendedor vendedor = Vendedor.crear("v1", "Distribuidor Oficial", "AUTORIZADO");
        return Componente.crear(id, "pieza-" + id, CategoriaComponente.GPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"),
                new Precio(new BigDecimal("100000"), "COP"), vendedor, 365);
    }

    @Test
    void elCompradorPuedeReservarUnaUnidadEnPreventa() {
        ReservaPreventa reserva = reservarPreventa.ejecutar(
                componente.getId(), unidadPreventa.getId(), "comprador-1");

        assertEquals(EstadoReservaPreventa.ACTIVA, reserva.getEstado());
        assertEquals(EstadoUnidad.PREVENTA_RESERVADA, unidadPreventa.getEstado());
        assertEquals(reserva.getId(), unidadPreventa.getReservaPreventaId());
        assertEquals(1, reservaRepository.listarTodas().size());
    }

    @Test
    void unaUnidadEnPreventaNoPuedeReservarseDosVeces() {
        reservarPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "comprador-1");

        assertThrows(ReglaDominioException.class,
                () -> reservarPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "comprador-2"));
    }

    @Test
    void cancelarLaReservaAntesDeLaLlegadaDevuelveLaUnidadAPreventa() {
        ReservaPreventa reserva = reservarPreventa.ejecutar(
                componente.getId(), unidadPreventa.getId(), "comprador-1");

        cancelarReservaPreventa.ejecutar(reserva.getId(), "comprador-1");

        assertEquals(EstadoReservaPreventa.CANCELADA, reserva.getEstado());
        assertEquals(EstadoUnidad.PREVENTA, unidadPreventa.getEstado());
        assertNull(unidadPreventa.getReservaPreventaId());
        assertEquals(1L, componente.stockPreventa());
    }

    @Test
    void recibirUnaPreventaReservadaLaDejaReservadaParaElComprador() {
        ReservaPreventa reserva = reservarPreventa.ejecutar(
                componente.getId(), unidadPreventa.getId(), "comprador-1");

        recibirPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "v1", "SER-LLEGADA");

        assertEquals(EstadoUnidad.RESERVADA, unidadPreventa.getEstado());
        assertEquals("SER-LLEGADA", unidadPreventa.getNumeroSerie());
        assertTrue(reserva.estaActiva());
        assertEquals(0L, componente.stockDisponible());
    }

    @Test
    void cancelarLaReservaDespuesDeLaLlegadaDevuelveLaUnidadAlStock() {
        ReservaPreventa reserva = reservarPreventa.ejecutar(
                componente.getId(), unidadPreventa.getId(), "comprador-1");
        recibirPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "v1", "SER-LLEGADA");

        cancelarReservaPreventa.ejecutar(reserva.getId(), "comprador-1");

        assertEquals(EstadoReservaPreventa.CANCELADA, reserva.getEstado());
        assertEquals(EstadoUnidad.DISPONIBLE, unidadPreventa.getEstado());
        assertNull(unidadPreventa.getReservaPreventaId());
        assertEquals(1L, componente.stockDisponible());
    }

    @Test
    void recibirUnaPreventaSinReservaLaDejaDisponible() {
        recibirPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "v1", "SER-LLEGADA");

        assertEquals(EstadoUnidad.DISPONIBLE, unidadPreventa.getEstado());
        assertEquals(1L, componente.stockDisponible());
    }

    @Test
    void recibirUnaPreventaExigeNumeroDeSerie() {
        assertThrows(ReglaDominioException.class,
                () -> recibirPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "v1", " "));
    }

    @Test
    void elNumeroDeSerieEsUnicoEnTodoElCatalogo() {
        Componente otro = crearComponente("comp-2");
        otro.agregarUnidad("SER-1");
        componenteRepository.guardar(otro);

        assertThrows(ReglaDominioException.class,
                () -> recibirPreventa.ejecutar(componente.getId(), unidadPreventa.getId(), "v1", "SER-1"));
    }

    @Test
    void noSePuedeReservarUnaUnidadQueNoEstaEnPreventa() {
        Componente disponible = crearComponente("comp-2");
        UnidadInventario unidad = disponible.agregarUnidad("SER-D");
        componenteRepository.guardar(disponible);

        assertThrows(ReglaDominioException.class,
                () -> reservarPreventa.ejecutar(disponible.getId(), unidad.getId(), "comprador-1"));
    }

    @Test
    void noSePuedeCancelarDosVecesLaMismaReserva() {
        ReservaPreventa reserva = reservarPreventa.ejecutar(
                componente.getId(), unidadPreventa.getId(), "comprador-1");
        cancelarReservaPreventa.ejecutar(reserva.getId(), "comprador-1");

        assertThrows(ReglaDominioException.class,
                () -> cancelarReservaPreventa.ejecutar(reserva.getId(), "comprador-1"));
    }

    @Test
    void soloElCompradorQueReservoPuedeCancelarLaReserva() {
        ReservaPreventa reserva = reservarPreventa.ejecutar(
                componente.getId(), unidadPreventa.getId(), "comprador-1");

        assertThrows(ReglaDominioException.class,
                () -> cancelarReservaPreventa.ejecutar(reserva.getId(), "comprador-2"));
        assertTrue(reserva.estaActiva());
        assertEquals(EstadoUnidad.PREVENTA_RESERVADA, unidadPreventa.getEstado());
    }
}
