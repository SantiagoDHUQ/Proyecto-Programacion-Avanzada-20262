package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.SolicitarRMARequest;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.RmaService;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoSolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RMAUseCaseTest {

    private static final String VENDEDOR_ID = "vendedor-1";
    private static final String COMPRADOR_ID = "comprador-1";
    private static final String COMPONENTE_ID = "componente-1";
    private static final String COMPRA_ID = "compra-1";
    private static final String SERIAL = "SER-1";
    private static final String RMA_ID = "rma-1";
    private static final int GARANTIA_DIAS = 365;

    @Mock
    private ComponenteRepository componenteRepository;

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private SolicitudRMARepository solicitudRepository;

    @Spy
    private RmaService rmaService = new RmaService();

    @InjectMocks
    private SolicitarRMAUseCase solicitarRMA;

    @InjectMocks
    private AceptarRMAUseCase aceptarRMA;

    @InjectMocks
    private RechazarRMAUseCase rechazarRMA;

    @InjectMocks
    private ConsultarRMAUseCase consultarRMA;

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

        lenient().when(solicitudRepository.guardar(any(SolicitudRMA.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(componenteRepository.guardar(any(Componente.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(compraRepository.obtenerComponenteIdPorNumeroSerie(any(), any())).thenCallRealMethod();
        lenient().when(compraRepository.obtenerItemPorNumeroSerie(any(), any())).thenCallRealMethod();
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
        // Arrange
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.empty());
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorId(COMPONENTE_ID)).thenReturn(Optional.of(componente));
        when(solicitudRepository.existePendientePorNumeroSerie(SERIAL)).thenReturn(false);

        // Act
        SolicitudRMA solicitud = solicitarRMA.ejecutar(request(RMA_ID));

        // Assert
        assertEquals(EstadoSolicitudRMA.PENDIENTE, solicitud.getEstado());
        assertEquals(SERIAL, solicitud.getNumeroSerie());
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
        verify(solicitudRepository).guardar(any(SolicitudRMA.class));
    }

    @Test
    void noSePuedeSolicitarRMAdeUnComponenteFueraDeGarantia() {
        // Arrange
        String otraCompraId = "compra-vencida";
        String otroSerial = "SER-VENCIDO";
        String otroComponenteId = "componente-vencido";
        Vendedor vendedor = Vendedor.crear(VENDEDOR_ID, "Distribuidor Oficial", "AUTORIZADO");
        Componente otroComponente = Componente.crear(
                otroComponenteId,
                "CPU 2",
                CategoriaComponente.CPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"),
                precio(),
                vendedor,
                GARANTIA_DIAS);
        UnidadInventario otraUnidad = otroComponente.agregarUnidad(VENDEDOR_ID, otroSerial, false);
        otroComponente.reservarUnidad(otraUnidad.getId());
        otroComponente.venderUnidad(otraUnidad.getId());

        Compra otraCompra = Compra.realizar(otraCompraId, null, COMPRADOR_ID,
                List.of(item(otraUnidad, otroComponenteId, otroSerial)));
        otraCompra.confirmar();

        when(solicitudRepository.buscarPorId("rma-2")).thenReturn(Optional.empty());
        when(compraRepository.buscarPorId(otraCompraId)).thenReturn(Optional.of(otraCompra));
        when(componenteRepository.buscarPorId(otroComponenteId)).thenReturn(Optional.of(otroComponente));
        when(solicitudRepository.existePendientePorNumeroSerie(otroSerial)).thenReturn(false);

        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> solicitarRMA.ejecutar(
                new SolicitarRMARequest("rma-2", otraCompraId, otroSerial, COMPRADOR_ID,
                        LocalDate.now().plusDays(400))));
    }

    @Test
    void noSePuedeSolicitarRMAdeUnComponenteSinGarantia() {
        // Arrange
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
        UnidadInventario unidadSinGarantia = sinGarantia.agregarUnidad("vendedor-particular", serial, false);
        sinGarantia.reservarUnidad(unidadSinGarantia.getId());
        sinGarantia.venderUnidad(unidadSinGarantia.getId());

        Compra compraSinGarantia = Compra.realizar(compraId, null, COMPRADOR_ID,
                List.of(item(unidadSinGarantia, componenteId, serial)));
        compraSinGarantia.confirmar();

        when(solicitudRepository.buscarPorId("rma-particular")).thenReturn(Optional.empty());
        when(compraRepository.buscarPorId(compraId)).thenReturn(Optional.of(compraSinGarantia));
        when(componenteRepository.buscarPorId(componenteId)).thenReturn(Optional.of(sinGarantia));
        when(solicitudRepository.existePendientePorNumeroSerie(serial)).thenReturn(false);

        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> solicitarRMA.ejecutar(
                new SolicitarRMARequest("rma-particular", compraId, serial, COMPRADOR_ID,
                        LocalDate.now())));
    }

    @Test
    void noSePuedeTenerDosSolicitudesPendientesParaLaMismaUnidad() {
        // Arrange
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.empty());
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorId(COMPONENTE_ID)).thenReturn(Optional.of(componente));
        when(solicitudRepository.existePendientePorNumeroSerie(SERIAL)).thenReturn(false);

        solicitarRMA.ejecutar(request(RMA_ID));

        when(solicitudRepository.buscarPorId("rma-2")).thenReturn(Optional.empty());
        when(solicitudRepository.existePendientePorNumeroSerie(SERIAL)).thenReturn(true);

        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> solicitarRMA.ejecutar(request("rma-2")));
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
    }

    @Test
    void elVendedorDuenoPuedeAceptarLaSolicitud() {
        // Arrange
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.empty());
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorId(COMPONENTE_ID)).thenReturn(Optional.of(componente));
        when(solicitudRepository.existePendientePorNumeroSerie(SERIAL)).thenReturn(false);

        SolicitudRMA solicitud = solicitarRMA.ejecutar(request(RMA_ID));
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.of(solicitud));

        // Act
        SolicitudRMA aceptada = aceptarRMA.ejecutar(RMA_ID, VENDEDOR_ID);

        // Assert
        assertEquals(EstadoSolicitudRMA.ACEPTADA, aceptada.getEstado());
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
    }

    @Test
    void unVendedorAjenoNoPuedeAceptarNiRechazarLaSolicitud() {
        // Arrange
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.empty());
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorId(COMPONENTE_ID)).thenReturn(Optional.of(componente));
        when(solicitudRepository.existePendientePorNumeroSerie(SERIAL)).thenReturn(false);

        SolicitudRMA solicitud = solicitarRMA.ejecutar(request(RMA_ID));
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.of(solicitud));

        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> aceptarRMA.ejecutar(RMA_ID, "otro-vendedor"));
        assertThrows(ReglaDominioException.class,
                () -> rechazarRMA.ejecutar(RMA_ID, "otro-vendedor", "Falla"));
        assertEquals(EstadoSolicitudRMA.PENDIENTE, solicitud.getEstado());
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
    }

    @Test
    void elVendedorDuenoPuedeRechazarYLaUnidadVuelveAVendida() {
        // Arrange
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.empty());
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorId(COMPONENTE_ID)).thenReturn(Optional.of(componente));
        when(solicitudRepository.existePendientePorNumeroSerie(SERIAL)).thenReturn(false);

        SolicitudRMA solicitud = solicitarRMA.ejecutar(request(RMA_ID));
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.of(solicitud));

        // Act
        SolicitudRMA rechazada = rechazarRMA.ejecutar(RMA_ID, VENDEDOR_ID, "Daño no cubierto");

        // Assert
        assertEquals(EstadoSolicitudRMA.RECHAZADA, rechazada.getEstado());
        assertEquals("Daño no cubierto", rechazada.getMotivoRechazo());
        assertEquals(EstadoUnidad.VENDIDA, unidad.getEstado());
    }

    @Test
    void rechazarSinMotivoNoCambiaElEstadoDeLaSolicitudNiDeLaUnidad() {
        // Arrange
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.empty());
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorId(COMPONENTE_ID)).thenReturn(Optional.of(componente));
        when(solicitudRepository.existePendientePorNumeroSerie(SERIAL)).thenReturn(false);

        SolicitudRMA solicitud = solicitarRMA.ejecutar(request(RMA_ID));
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.of(solicitud));

        // Act & Assert
        assertThrows(ReglaDominioException.class,
                () -> rechazarRMA.ejecutar(RMA_ID, VENDEDOR_ID, "   "));
        assertEquals(EstadoSolicitudRMA.PENDIENTE, solicitud.getEstado());
        assertEquals(EstadoUnidad.EN_RMA, unidad.getEstado());
    }

    @Test
    void unaSolicitudAceptadaNoPuedeAceptarseDosVeces() {
        // Arrange
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.empty());
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorId(COMPONENTE_ID)).thenReturn(Optional.of(componente));
        when(solicitudRepository.existePendientePorNumeroSerie(SERIAL)).thenReturn(false);

        SolicitudRMA solicitud = solicitarRMA.ejecutar(request(RMA_ID));
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.of(solicitud));
        aceptarRMA.ejecutar(RMA_ID, VENDEDOR_ID);

        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> aceptarRMA.ejecutar(RMA_ID, VENDEDOR_ID));
        assertEquals(EstadoSolicitudRMA.ACEPTADA, solicitud.getEstado());
    }

    @Test
    void consultarRMAExistenteYNoExistente() {
        // Arrange
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.empty());
        when(compraRepository.buscarPorId(COMPRA_ID)).thenReturn(Optional.of(compra));
        when(componenteRepository.buscarPorId(COMPONENTE_ID)).thenReturn(Optional.of(componente));
        when(solicitudRepository.existePendientePorNumeroSerie(SERIAL)).thenReturn(false);

        SolicitudRMA solicitud = solicitarRMA.ejecutar(request(RMA_ID));
        when(solicitudRepository.buscarPorId(RMA_ID)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.buscarPorId("rma-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        assertEquals(RMA_ID, consultarRMA.ejecutar(RMA_ID).getId());
        assertThrows(RecursoNoEncontradoException.class,
                () -> consultarRMA.ejecutar("rma-inexistente"));
    }
}
