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
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CompraService;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.RmaService;
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
    private RmaService rmaService;
    private CompraService compraService;
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
        SolicitudRMARepositoryEnMemoria solicitudRepository = new SolicitudRMARepositoryEnMemoria();
        RmaService rmaService = new RmaService();
        CompraService compraService = new CompraService(java.time.Clock.systemUTC());

        SolicitarRMAUseCase solicitarRMA = new SolicitarRMAUseCase(new CompraRepositoryEnMemoria(), new ComponenteRepositoryEnMemoria(), solicitudRepository, new RmaService());
        AceptarRMAUseCase aceptarRMA = new AceptarRMAUseCase(solicitudRepository, new ComponenteRepositoryEnMemoria(), new RmaService());
        ReembolsarCompraUseCase reembolsarCompra = new ReembolsarCompraUseCase(
                compraRepository, componenteRepository, solicitudRepository, compraService);

        Vendedor vendedor = Vendedor.crear(VENDEDOR_ID, "Distribuidor Oficial", "AUTORIZADO");
        componente = Componente.crear(
                COMPONENTE_ID,
                "CPU",
                CategoriaComponente.CPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"),
                new Precio(new BigDecimal("100000"), "COP"), vendedor, GARANTIA_DIAS);
        unidad = componente.agregarUnidad(VENDEDOR_ID, SERIAL, false);
        componente.reservarUnidad(unidad.getId());
        componente.venderUnidad(unidad.getId());
        componenteRepository.guardar(componente);

        compra = Compra.realizar(COMPRA_ID, null, COMPRADOR_ID,
                List.of(new ItemCompra(unidad.getId(), COMPONENTE_ID, SERIAL, precio())));
        compra.confirmar();
        compraRepository.guardar(compra);
    }

    private void aceptarRMA(String compraId, String rmaId, String numeroSerie) {
        SolicitarRMAUseCase solicitarRMA = new SolicitarRMAUseCase(new CompraRepositoryEnMemoria(), new ComponenteRepositoryEnMemoria(), new SolicitudRMARepositoryEnMemoria(), new RmaService());
        AceptarRMAUseCase aceptarRMA = new AceptarRMAUseCase(new SolicitudRMARepositoryEnMemoria(), new ComponenteRepositoryEnMemoria(), new RmaService());
        solicitarRMA.ejecutar(new SolicitarRMARequest(
                rmaId, compraId, numeroSerie, COMPRADOR_ID, LocalDate.now()));
        aceptarRMA.ejecutar(rmaId, VENDEDOR_ID);
    }

    private void rechazarRMA(String compraId, String rmaId, String numeroSerie) {
        SolicitarRMAUseCase solicitarRMA = new SolicitarRMAUseCase(new CompraRepositoryEnMemoria(), new ComponenteRepositoryEnMemoria(), new SolicitudRMARepositoryEnMemoria(), new RmaService());
        RechazarRMAUseCase rechazarRMA = new RechazarRMAUseCase(new SolicitudRMARepositoryEnMemoria(), new ComponenteRepositoryEnMemoria(), new RmaService());
        solicitarRMA.ejecutar(new SolicitarRMARequest(
                rmaId, compraId, numeroSerie, COMPRADOR_ID, LocalDate.now()));
        rechazarRMA.ejecutar(rmaId, VENDEDOR_ID, "Falla no cubierta");
    }

    private Precio precio() {
        return new Precio(new BigDecimal("100000"), "COP");
    }

    private ItemCompra item(UnidadInventario unidad, String componenteId, String numeroSerie) {
        return new ItemCompra(unidad.getId(), componenteId, numeroSerie, precio());
    }
}