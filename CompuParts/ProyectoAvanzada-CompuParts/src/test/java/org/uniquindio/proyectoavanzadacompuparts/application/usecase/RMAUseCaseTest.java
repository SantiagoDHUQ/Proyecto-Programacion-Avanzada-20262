package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.SolicitarRMARequest;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoSolicitudRMA;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RMAUseCaseTest {

    private static final String VENDEDOR_ID = "vendedor-1";
    private static final String COMPRADOR_ID = "comprador-1";
    private static final String COMPONENTE_ID = "componente-1";
    private static final String COMPRA_ID = "compra-1";
    private static final String SERIAL = "SER-1";
    private static final String RMA_ID = "rma-1";
    private static final int GARANTIA_DIAS = 365;

    private ComponenteRepositoryEnMemoria componenteRepository;
    private CompraRepositoryEnMemoria compraRepository;
    private SolicitudRMARepositoryEnMemoria solicitudRepository;
    private SolicitarRMAUseCase solicitarRMA;
    private AceptarRMAUseCase aceptarRMA;
    private RechazarRMAUseCase rechazarRMA;
    private ConsultarRMAUseCase consultarRMA;
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
        rechazarRMA = new RechazarRMAUseCase(solicitudRepository, componenteRepository);
        consultarRMA = new ConsultarRMAUseCase(solicitudRepository);

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

    private Precio precio() {
        return new Precio(new BigDecimal("100000"), "COP");
    }

    private ItemCompra item(UnidadInventario unidad, String componenteId, String numeroSerie) {
        return new ItemCompra(unidad.getId(), componenteId, numeroSerie, precio());
    }

    private SolicitarRMARequest request(String id) {
        return new SolicitarRMARequest(id, COMPRA_ID, SERIAL, COMPRADOR_ID, LocalDate.now());
    }

    @Test
    void solicitarRMACambiaUnidadAEnRMAYPersistePendiente() {
        SolicitudRMA solicitud = solicitarRMA.ejecutar(request(RMA_ID));

        assertEquals(EstadoSolicitudRMA.PENDIENTE, solicitud.getEstado());
        assertEquals(SERIAL, solicitud.getNumeroSerie());
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
        assertTrue(solicitudRepository.buscarPorId(RMA_ID).isPresent());
    }

    @Test
    void noSePuedeSolicitarRMAdeUnaCompraPendiente() {
        Compra pendiente = Compra.realizar("compra-pendiente", null, COMPRADOR_ID,
                List.of(item(unidad, COMPONENTE_ID, SERIAL)));
        compraRepository.guardar(pendiente);

        assertThrows(ReglaDominioException.class, () -> solicitarRMA.ejecutar(
                new SolicitarRMARequest("rma-pendiente", "compra-pendiente", SERIAL,
                        COMPRADOR_ID, LocalDate.now())));
    }

    @Test
    void noSePuedeSolicitarRMAdeOtroComprador() {
        assertThrows(ReglaDominioException.class, () -> solicitarRMA.ejecutar(
                new SolicitarRMARequest(RMA_ID, COMPRA_ID, SERIAL, "otro-comprador",
                        LocalDate.now())));
    }

    @Test
    void noSePuedeSolicitarRMAConUnSerialAjenoALaCompra() {
        assertThrows(ReglaDominioException.class, () -> solicitarRMA.ejecutar(
                new SolicitarRMARequest(RMA_ID, COMPRA_ID, "SER-OTRO", COMPRADOR_ID,
                        LocalDate.now())));
    }

    @Test
    void noSePuedeSolicitarRMAdeUnaUnidadQueNoEstaVendida() {
        String otroComponenteId = "componente-2";
        String otroSerial = "SER-2";
        String otraCompraId = "compra-2";
        Vendedor vendedor = Vendedor.crear("vendedor-2", "Distribuidor", "AUTORIZADO");
        Componente otroComponente = Componente.crear(
                otroComponenteId,
                "CPU 2",
                CategoriaComponente.CPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"),
                precio(),
                vendedor,
                GARANTIA_DIAS);
        UnidadInventario otraUnidad = otroComponente.agregarUnidad(otroSerial);
        componenteRepository.guardar(otroComponente);

        Compra otraCompra = Compra.realizar(otraCompraId, null, COMPRADOR_ID,
                List.of(item(otraUnidad, otroComponenteId, otroSerial)));
        otraCompra.confirmar();
        compraRepository.guardar(otraCompra);

        assertThrows(ReglaDominioException.class, () -> solicitarRMA.ejecutar(
                new SolicitarRMARequest("rma-2", otraCompraId, otroSerial, COMPRADOR_ID,
                        LocalDate.now())));
    }

    @Test
    void noSePuedeSolicitarRMAdeUnComponenteSinGarantia() {
        String componenteId = "componente-particular";
        String serial = "SER-PARTICULAR";
        String compraId = "compra-particular";
        Vendedor particular = Vendedor.crear("vendedor-particular", "Juan", "PARTICULAR");
        Componente sinGarantia = Componente.crear(
                componenteId,
                "GPU usada",
                CategoriaComponente.GPU,
                new EspecificacionTecnica(null, 250, null),
                precio(),
                particular,
                null);
        UnidadInventario unidadSinGarantia = sinGarantia.agregarUnidad(serial);
        sinGarantia.reservarUnidad(unidadSinGarantia.getId());
        sinGarantia.venderUnidad(unidadSinGarantia.getId());
        componenteRepository.guardar(sinGarantia);

        Compra compraSinGarantia = Compra.realizar(compraId, null, COMPRADOR_ID,
                List.of(item(unidadSinGarantia, componenteId, serial)));
        compraSinGarantia.confirmar();
        compraRepository.guardar(compraSinGarantia);

        assertThrows(ReglaDominioException.class, () -> solicitarRMA.ejecutar(
                new SolicitarRMARequest("rma-particular", compraId, serial, COMPRADOR_ID,
                        LocalDate.now())));
    }

    @Test
    void noSePuedeTenerDosSolicitudesPendientesParaLaMismaUnidad() {
        solicitarRMA.ejecutar(request(RMA_ID));

        assertThrows(ReglaDominioException.class, () -> solicitarRMA.ejecutar(request("rma-2")));
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
    }

    @Test
    void elVendedorDuenoPuedeAceptarLaSolicitud() {
        solicitarRMA.ejecutar(request(RMA_ID));

        SolicitudRMA aceptada = aceptarRMA.ejecutar(RMA_ID, VENDEDOR_ID);

        assertEquals(EstadoSolicitudRMA.ACEPTADA, aceptada.getEstado());
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
    }

    @Test
    void unVendedorAjenoNoPuedeAceptarNiRechazarLaSolicitud() {
        solicitarRMA.ejecutar(request(RMA_ID));

        assertThrows(ReglaDominioException.class, () -> aceptarRMA.ejecutar(RMA_ID, "otro-vendedor"));
        assertThrows(ReglaDominioException.class,
                () -> rechazarRMA.ejecutar(RMA_ID, "otro-vendedor", "Falla"));

        assertEquals(EstadoSolicitudRMA.PENDIENTE,
                solicitudRepository.buscarPorId(RMA_ID).orElseThrow().getEstado());
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
    }

    @Test
    void elVendedorDuenoPuedeRechazarYLaUnidadVuelveAVendida() {
        solicitarRMA.ejecutar(request(RMA_ID));

        SolicitudRMA rechazada = rechazarRMA.ejecutar(RMA_ID, VENDEDOR_ID, "Daño no cubierto");

        assertEquals(EstadoSolicitudRMA.RECHAZADA, rechazada.getEstado());
        assertEquals("Daño no cubierto", rechazada.getMotivoRechazo());
        assertEquals(EstadoUnidad.VENDIDA, unidad.getEstado());
    }

    @Test
    void rechazarSinMotivoNoCambiaElEstadoDeLaSolicitudNiDeLaUnidad() {
        solicitarRMA.ejecutar(request(RMA_ID));

        assertThrows(ReglaDominioException.class,
                () -> rechazarRMA.ejecutar(RMA_ID, VENDEDOR_ID, "   "));

        assertEquals(EstadoSolicitudRMA.PENDIENTE,
                solicitudRepository.buscarPorId(RMA_ID).orElseThrow().getEstado());
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
    }

    @Test
    void unaSolicitudAceptadaNoPuedeAceptarseDosVeces() {
        solicitarRMA.ejecutar(request(RMA_ID));
        aceptarRMA.ejecutar(RMA_ID, VENDEDOR_ID);

        assertThrows(ReglaDominioException.class, () -> aceptarRMA.ejecutar(RMA_ID, VENDEDOR_ID));
        assertEquals(EstadoSolicitudRMA.ACEPTADA,
                solicitudRepository.buscarPorId(RMA_ID).orElseThrow().getEstado());
    }

    @Test
    void consultarRMAExistenteYNoExistente() {
        solicitarRMA.ejecutar(request(RMA_ID));

        assertEquals(RMA_ID, consultarRMA.ejecutar(RMA_ID).getId());
        assertThrows(RecursoNoEncontradoException.class,
                () -> consultarRMA.ejecutar("rma-inexistente"));
    }
}
