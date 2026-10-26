package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoComercial;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ComponenteTest {

    // Arrange general
    private final Vendedor autorizado = Vendedor.crear("v1", "Distribuidor Oficial", "AUTORIZADO");
    private final Vendedor particular = Vendedor.crear("v2", "Juan (usado)", "PARTICULAR");
    private final EspecificacionTecnica spec = new EspecificacionTecnica("AM5", 100, "DDR5");
    private final Precio precio = new Precio(new BigDecimal("1500000"), "COP");

    private Componente crear(String id, Vendedor vendedor, Integer garantiaDias) {
        return Componente.crear(id, "RTX 4070", CategoriaComponente.GPU, spec, precio, vendedor, garantiaDias);
    }

    private Componente conUnidad(String id, Vendedor vendedor, Integer garantiaDias, String serial) {
        Componente componente = crear(id, vendedor, garantiaDias);
        componente.agregarUnidad(serial);
        return componente;
    }

    // ---------- Creación y garantía ----------

    @Test
    void vendedorAutorizadoConGarantiaCreaComponente() {
        Componente componente = crear("1", autorizado, 365);

        assertEquals(EstadoComercial.PUBLICADO, componente.getEstadoComercial());
        assertEquals(365, componente.getGarantia().orElseThrow().dias());
        assertEquals(0L, componente.stockDisponible());
        assertFalse(componente.estaDisponibleParaVenta());
    }

    @Test
    void vendedorAutorizadoSinGarantiaSeRechaza() {
        assertThrows(ReglaDominioException.class, () -> crear("1", autorizado, null));
    }

    @Test
    void vendedorParticularConGarantiaSeRechaza() {
        assertThrows(ReglaDominioException.class, () -> crear("1", particular, 30));
    }

    @Test
    void vendedorParticularSinGarantiaCreaComponenteUsado() {
        Componente componente = crear("1", particular, null);

        assertTrue(componente.getGarantia().isEmpty());
    }

    @Test
    void datosObligatoriosFaltantesLanzanReglaDominio() {
        assertThrows(ReglaDominioException.class, () -> crear("", autorizado, 30));
        assertThrows(ReglaDominioException.class, () -> crear("1", null, 30));
        assertThrows(ReglaDominioException.class,
                () -> Componente.crear("1", " ", CategoriaComponente.GPU, spec, precio, autorizado, 30));
        assertThrows(ReglaDominioException.class,
                () -> Componente.crear("1", "x", CategoriaComponente.GPU, null, precio, autorizado, 30));
        assertThrows(ReglaDominioException.class,
                () -> Componente.crear("1", "x", CategoriaComponente.GPU, spec, null, autorizado, 30));
    }

    // ---------- Stock ----------

    @Test
    void agregarUnidadDisponibleAumentaElStock() {
        Componente componente = crear("1", autorizado, 365);

        UnidadInventario unidad = componente.agregarUnidad("GPU-1");

        assertEquals(EstadoUnidad.DISPONIBLE, unidad.getEstado());
        assertEquals("GPU-1", unidad.getNumeroSerie());
        assertEquals(1L, componente.stockDisponible());
        assertTrue(componente.estaDisponibleParaVenta());
        assertEquals(1, componente.getUnidades().size());
        assertTrue(componente.existeNumeroSerie("GPU-1"));
    }

    @Test
    void noSePuedeAgregarDosUnidadesConElMismoSerial() {
        Componente componente = conUnidad("1", autorizado, 365, "SER-1");

        assertThrows(ReglaDominioException.class, () -> componente.agregarUnidad("SER-1"));
    }

    @Test
    void noSePuedeAgregarUnaUnidadSinSerial() {
        Componente componente = crear("1", autorizado, 365);

        assertThrows(ReglaDominioException.class, () -> componente.agregarUnidad(" "));
    }

    @Test
    void reservarUnaUnidadLaSacaDelStock() {
        Componente componente = conUnidad("1", autorizado, 365, "SER-1");
        UnidadInventario unidad = componente.getUnidades().getFirst();

        componente.reservarUnidad(unidad.getId());

        assertEquals(EstadoUnidad.RESERVADA, unidad.getEstado());
        assertEquals(0L, componente.stockDisponible());
        assertFalse(componente.estaDisponibleParaVenta());
    }

    @Test
    void venderUnaUnidadReservadaLaDejaVendida() {
        Componente componente = conUnidad("1", autorizado, 365, "SER-1");
        UnidadInventario unidad = componente.getUnidades().getFirst();
        componente.reservarUnidad(unidad.getId());

        componente.venderUnidad(unidad.getId());

        assertEquals(EstadoUnidad.VENDIDA, unidad.getEstado());
        assertEquals(0L, componente.stockDisponible());
    }

    @Test
    void noSePuedeVenderUnaUnidadSinReservar() {
        Componente componente = conUnidad("1", autorizado, 365, "SER-1");
        UnidadInventario unidad = componente.getUnidades().getFirst();

        assertThrows(ReglaDominioException.class, () -> componente.venderUnidad(unidad.getId()));
    }

    @Test
    void liberarReservaDevuelveLaUnidadAlStock() {
        Componente componente = conUnidad("1", autorizado, 365, "SER-1");
        UnidadInventario unidad = componente.getUnidades().getFirst();
        componente.reservarUnidad(unidad.getId());

        componente.liberarReservaCompra(unidad.getId());

        assertEquals(EstadoUnidad.DISPONIBLE, unidad.getEstado());
        assertEquals(1L, componente.stockDisponible());
    }

    @Test
    void reservarPrimeraUnidadDisponibleFallaSiNoHayStock() {
        Componente componente = crear("1", autorizado, 365);

        assertThrows(ReglaDominioException.class, componente::reservarPrimeraUnidadDisponible);
    }

    @Test
    void descartarUnaUnidadNoLaDevuelveAlStock() {
        Componente componente = conUnidad("1", autorizado, 365, "SER-1");
        UnidadInventario unidad = componente.getUnidades().getFirst();

        componente.descartarUnidad(unidad.getId());

        assertEquals(EstadoUnidad.DESCARTADA, unidad.getEstado());
        assertEquals(0L, componente.stockDisponible());
    }

    @Test
    void unaUnidadVendidaPuedeEntrarEnRMAYRechazarse() {
        Componente componente = conUnidad("1", autorizado, 365, "SER-1");
        UnidadInventario unidad = componente.getUnidades().getFirst();
        componente.reservarUnidad(unidad.getId());
        componente.venderUnidad(unidad.getId());

        componente.entrarEnRMA(unidad.getId());
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());

        unidad.rechazarRMA();
        assertEquals(EstadoUnidad.VENDIDA, unidad.getEstado());
    }

    // ---------- Preventa ----------

    @Test
    void preventaSinFechaEsRechazada() {
        Componente componente = crear("1", autorizado, 365);

        assertThrows(ReglaDominioException.class, () -> componente.agregarPreventa(null));
    }

    @Test
    void preventaConFechaPasadaEsRechazada() {
        Componente componente = crear("1", autorizado, 365);

        assertThrows(ReglaDominioException.class,
                () -> componente.agregarPreventa(LocalDate.now().minusDays(1)));
    }

    @Test
    void preventaConFechaFuturaEsAceptada() {
        Componente componente = crear("1", autorizado, 365);
        LocalDate llegada = LocalDate.now().plusDays(10);

        UnidadInventario unidad = componente.agregarPreventa(llegada);

        assertEquals(EstadoUnidad.PREVENTA, unidad.getEstado());
        assertNull(unidad.getNumeroSerie());
        assertEquals(llegada, unidad.getFechaEstimadaLlegada());
        assertEquals(1L, componente.stockPreventa());
        assertEquals(0L, componente.stockDisponible());
        assertFalse(componente.estaDisponibleParaVenta());
    }

    @Test
    void particularNoPuedePublicarPreventa() {
        Componente componente = crear("1", particular, null);

        assertThrows(ReglaDominioException.class,
                () -> componente.agregarPreventa(LocalDate.now().plusDays(10)));
    }

    @Test
    void reservarPreventaYLuegoRecibirLaDejaReservadaParaElComprador() {
        Componente componente = crear("1", autorizado, 365);
        UnidadInventario unidad = componente.agregarPreventa(LocalDate.now().plusDays(10));
        UUID reservaId = UUID.randomUUID();

        componente.reservarPreventa(unidad.getId(), reservaId);
        assertEquals(EstadoUnidad.PREVENTA_RESERVADA, unidad.getEstado());
        assertEquals(reservaId, unidad.getReservaPreventaId());

        componente.recibirPreventa(unidad.getId(), "SER-LLEGADA");

        assertEquals(EstadoUnidad.RESERVADA, unidad.getEstado());
        assertEquals("SER-LLEGADA", unidad.getNumeroSerie());
        assertEquals(reservaId, unidad.getReservaPreventaId());
        assertNull(unidad.getFechaEstimadaLlegada());
        assertEquals(0L, componente.stockDisponible());
    }

    @Test
    void recibirUnaPreventaSinReservaLaDejaDisponible() {
        Componente componente = crear("1", autorizado, 365);
        UnidadInventario unidad = componente.agregarPreventa(LocalDate.now().plusDays(10));

        componente.recibirPreventa(unidad.getId(), "SER-LLEGADA");

        assertEquals(EstadoUnidad.DISPONIBLE, unidad.getEstado());
        assertEquals(1L, componente.stockDisponible());
    }

    @Test
    void recibirPreventaExigeNumeroDeSerie() {
        Componente componente = crear("1", autorizado, 365);
        UnidadInventario unidad = componente.agregarPreventa(LocalDate.now().plusDays(10));

        assertThrows(ReglaDominioException.class, () -> componente.recibirPreventa(unidad.getId(), " "));
    }

    @Test
    void liberarReservaPreventaLaDevuelveAPreventa() {
        Componente componente = crear("1", autorizado, 365);
        UnidadInventario unidad = componente.agregarPreventa(LocalDate.now().plusDays(10));
        componente.reservarPreventa(unidad.getId(), UUID.randomUUID());

        componente.liberarReservaPreventa(unidad.getId());

        assertEquals(EstadoUnidad.PREVENTA, unidad.getEstado());
        assertNull(unidad.getReservaPreventaId());
        assertEquals(1L, componente.stockPreventa());
    }

    @Test
    void liberarUnaCompraSobreUnaUnidadReservadaEnPreventaLaMantieneParaElComprador() {
        Componente componente = crear("1", autorizado, 365);
        UnidadInventario unidad = componente.agregarPreventa(LocalDate.now().plusDays(10));
        UUID reservaId = UUID.randomUUID();
        componente.reservarPreventa(unidad.getId(), reservaId);
        componente.recibirPreventa(unidad.getId(), "SER-LLEGADA");

        componente.liberarReservaCompra(unidad.getId());

        assertEquals(EstadoUnidad.RESERVADA, unidad.getEstado());
        assertEquals(reservaId, unidad.getReservaPreventaId());
        assertEquals(0L, componente.stockDisponible());
    }

    // ---------- Estado comercial ----------

    @Test
    void marcarAgotadoOcultaElComponenteSinBorrarSusUnidades() {
        Componente componente = conUnidad("1", autorizado, 365, "SER-1");

        componente.marcarAgotado();

        assertEquals(EstadoComercial.AGOTADO, componente.getEstadoComercial());
        assertEquals(1L, componente.stockDisponible());
        assertFalse(componente.estaDisponibleParaVenta());
        assertEquals(1, componente.getUnidades().size());
    }

    @Test
    void publicarVuelveAPublicarUnComponenteAgotado() {
        Componente componente = conUnidad("1", autorizado, 365, "SER-1");
        componente.marcarAgotado();

        componente.publicar();

        assertEquals(EstadoComercial.PUBLICADO, componente.getEstadoComercial());
        assertTrue(componente.estaDisponibleParaVenta());
    }

    @Test
    void eliminarEsBorradoLogicoYConservaLasUnidades() {
        Componente componente = conUnidad("1", autorizado, 365, "SER-1");

        componente.eliminar();

        assertEquals(EstadoComercial.ELIMINADO, componente.getEstadoComercial());
        assertTrue(componente.estaEliminado());
        assertFalse(componente.estaDisponibleParaVenta());
        assertEquals(1L, componente.stockDisponible());
        assertEquals(1, componente.getUnidades().size());
    }

    @Test
    void noSePuedeAgregarStockAUnComponenteEliminado() {
        Componente componente = crear("1", autorizado, 365);
        componente.eliminar();

        assertThrows(ReglaDominioException.class, () -> componente.agregarUnidad("SER-1"));
    }

    @Test
    void noSePuedePublicarUnComponenteEliminado() {
        Componente componente = crear("1", autorizado, 365);
        componente.eliminar();

        assertThrows(ReglaDominioException.class, componente::publicar);
    }

    // ---------- RMA / garantía ----------

    @Test
    void autorizadoDentroDeGarantiaAdmiteRMA() {
        Componente componente = crear("1", autorizado, 30);
        LocalDate compra = LocalDate.of(2026, 1, 1);

        assertTrue(componente.admiteRMA(compra, compra.plusDays(29)));
    }

    @Test
    void autorizadoElDiaDelVencimientoNoAdmiteRMA() {
        Componente componente = crear("1", autorizado, 30);
        LocalDate compra = LocalDate.of(2026, 1, 1);

        assertFalse(componente.admiteRMA(compra, compra.plusDays(30)));
    }

    @Test
    void componenteUsadoNuncaAdmiteRMA() {
        Componente componente = crear("1", particular, null);
        LocalDate compra = LocalDate.of(2026, 1, 1);

        assertFalse(componente.admiteRMA(compra, compra.plusDays(10)));
    }

    @Test
    void componentesConElMismoIdSonIguales() {
        Componente uno = crear("1", autorizado, 30);
        Componente otro = crear("1", particular, null);

        assertEquals(uno, otro);
        assertEquals(uno.hashCode(), otro.hashCode());
    }
}
