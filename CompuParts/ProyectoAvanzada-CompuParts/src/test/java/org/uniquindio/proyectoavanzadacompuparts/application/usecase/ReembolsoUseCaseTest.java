package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.SolicitarRMARequest;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.ComponenteRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.CompraRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.SolicitudRMARepositoryEnMemoria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReembolsoUseCaseTest {

    private static final String VENDEDOR_ID = "vendedor-1";
    private static final String COMPRADOR_ID = "comprador-1";
    private static final String COMPONENTE_ID = "componente-1";
    private static final String COMPRA_ID = "compra-1";
    private static final String SERIAL = "SER-1";
    private static final int GARANTIA_DIAS = 365;

    private ComponenteRepositoryEnMemoria componenteRepository;
    private CompraRepositoryEnMemoria compraRepository;
    private SolicitudRMARepositoryEnMemoria solicitudRepository;
    private SolicitarRMAUseCase solicitarRMA;
    private AceptarRMAUseCase aceptarRMA;
    private ReembolsarCompraUseCase reembolsarCompra;
    private Componente componente;
    private UnidadInventario unidad;
    private Compra compra;

    @BeforeEach
    void setUp() {
        componenteRepository = new ComponenteRepositoryEnMemoria();
        compraRepository = new CompraRepositoryEnMemoria();
        solicitudRepository = new SolicitudRMARepositoryEnMemoria();

        solicitarRMA = new SolicitarRMAUseCase(compraRepository, componenteRepository, solicitudRepository);
        aceptarRMA = new AceptarRMAUseCase(solicitudRepository, componenteRepository);
        reembolsarCompra = new ReembolsarCompraUseCase(
                compraRepository, componenteRepository, solicitudRepository);

        Vendedor vendedor = Vendedor.crear(VENDEDOR_ID, "Distribuidor Oficial", "AUTORIZADO");
        componente = Componente.crear(
                COMPONENTE_ID,
                "CPU",
                CategoriaComponente.CPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"),
                precio(),
                vendedor,
                GARANTIA_DIAS);
        unidad = componente.agregarUnidad(SERIAL);
        componente.reservarUnidad(unidad.getId());
        componente.venderUnidad(unidad.getId());
        componenteRepository.guardar(componente);

        compra = Compra.realizar(COMPRA_ID, null, COMPRADOR_ID,
                List.of(item(unidad, COMPONENTE_ID, SERIAL)));
        compra.confirmar();
        compraRepository.guardar(compra);
    }

    @Test
    void reembolsarUnaCompraConRMAAceptadaDescartaLaUnidad() {
        aceptarRMA(COMPRA_ID, "rma-1", SERIAL);

        Compra reembolsada = reembolsarCompra.ejecutar(COMPRA_ID, COMPRADOR_ID, "Falla de fábrica");

        assertEquals(EstadoCompra.REEMBOLSADA, reembolsada.getEstado());
        assertEquals(EstadoUnidad.DESCARTADA, unidad.getEstado());
        assertEquals(EstadoCompra.REEMBOLSADA,
                compraRepository.buscarPorId(COMPRA_ID).orElseThrow().getEstado());
    }

    @Test
    void noSePuedeReembolsarUnaCompraSinRMAAceptada() {
        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar(COMPRA_ID, COMPRADOR_ID, "Falla de fábrica"));

        assertEquals(EstadoCompra.COMPLETADA,
                compraRepository.buscarPorId(COMPRA_ID).orElseThrow().getEstado());
        assertEquals(EstadoUnidad.VENDIDA, unidad.getEstado());
    }

    @Test
    void noSePuedeReembolsarUnaCompraConRMAPendiente() {
        solicitarRMA.ejecutar(new SolicitarRMARequest(
                "rma-pendiente", COMPRA_ID, SERIAL, COMPRADOR_ID, LocalDate.now()));

        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar(COMPRA_ID, COMPRADOR_ID, "Falla de fábrica"));

        assertEquals(EstadoCompra.COMPLETADA,
                compraRepository.buscarPorId(COMPRA_ID).orElseThrow().getEstado());
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
    }

    @Test
    void soloElCompradorDeLaCompraPuedeReembolsarla() {
        aceptarRMA(COMPRA_ID, "rma-1", SERIAL);

        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar(COMPRA_ID, "otro-comprador", "Falla de fábrica"));

        assertEquals(EstadoCompra.COMPLETADA,
                compraRepository.buscarPorId(COMPRA_ID).orElseThrow().getEstado());
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
    }

    @Test
    void noSePuedeReembolsarUnaCompraPendiente() {
        Compra pendiente = Compra.realizar("compra-pendiente", null, COMPRADOR_ID,
                List.of(item(unidad, COMPONENTE_ID, SERIAL)));
        compraRepository.guardar(pendiente);

        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar("compra-pendiente", COMPRADOR_ID, "Falla de fábrica"));

        assertEquals(EstadoCompra.PENDIENTE,
                compraRepository.buscarPorId("compra-pendiente").orElseThrow().getEstado());
    }

    @Test
    void elReembolsoExigeMotivoYNoDescartaLaUnidad() {
        aceptarRMA(COMPRA_ID, "rma-1", SERIAL);

        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar(COMPRA_ID, COMPRADOR_ID, "   "));

        assertEquals(EstadoCompra.COMPLETADA,
                compraRepository.buscarPorId(COMPRA_ID).orElseThrow().getEstado());
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
    }

    @Test
    void unaCompraInexistenteNoSePuedeReembolsar() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> reembolsarCompra.ejecutar("compra-inexistente", COMPRADOR_ID, "Falla de fábrica"));
    }

    @Test
    void siUnaUnidadNoTieneRMAAceptadaNoSeDescartaNinguna() {
        UnidadInventario segundaUnidad = componente.agregarUnidad("SER-2");
        componente.reservarUnidad(segundaUnidad.getId());
        componente.venderUnidad(segundaUnidad.getId());
        componenteRepository.guardar(componente);

        Compra compraDosUnidades = Compra.realizar("compra-2", null, COMPRADOR_ID,
                List.of(item(unidad, COMPONENTE_ID, SERIAL),
                        item(segundaUnidad, COMPONENTE_ID, "SER-2")));
        compraDosUnidades.confirmar();
        compraRepository.guardar(compraDosUnidades);

        aceptarRMA("compra-2", "rma-1", SERIAL);

        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar("compra-2", COMPRADOR_ID, "Falla de fábrica"));

        assertEquals(EstadoCompra.COMPLETADA,
                compraRepository.buscarPorId("compra-2").orElseThrow().getEstado());
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
        assertEquals(EstadoUnidad.VENDIDA, segundaUnidad.getEstado());
    }

    @Test
    void unSegundoReembolsoNoCambiaUnaCompraYaReembolsada() {
        aceptarRMA(COMPRA_ID, "rma-1", SERIAL);
        reembolsarCompra.ejecutar(COMPRA_ID, COMPRADOR_ID, "Falla de fábrica");

        assertThrows(ReglaDominioException.class,
                () -> reembolsarCompra.ejecutar(COMPRA_ID, COMPRADOR_ID, "Otra vez"));

        assertEquals(EstadoCompra.REEMBOLSADA,
                compraRepository.buscarPorId(COMPRA_ID).orElseThrow().getEstado());
        assertEquals(EstadoUnidad.DESCARTADA, unidad.getEstado());
    }

    private void aceptarRMA(String compraId, String rmaId, String numeroSerie) {
        solicitarRMA.ejecutar(new SolicitarRMARequest(
                rmaId, compraId, numeroSerie, COMPRADOR_ID, LocalDate.now()));
        aceptarRMA.ejecutar(rmaId, VENDEDOR_ID);
    }

    private Precio precio() {
        return new Precio(new BigDecimal("100000"), "COP");
    }

    private ItemCompra item(UnidadInventario unidad, String componenteId, String numeroSerie) {
        return new ItemCompra(unidad.getId(), componenteId, numeroSerie, precio());
    }
}
