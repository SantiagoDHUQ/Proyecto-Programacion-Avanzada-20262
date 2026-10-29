package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.RechazarRMARequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.SolicitarRMARequest;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
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

import static org.junit.jupiter.api.Assertions.*;

public class RMAUseCaseTest {

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
        SolicitudRMARepositoryEnMemoria solicitudRepository = new SolicitudRMARepositoryEnMemoria();
        RmaService rmaService = new RmaService();

        SolicitarRMAUseCase solicitarRMA = new SolicitarRMAUseCase(compraRepository, new ComponenteRepositoryEnMemoria(), solicitudRepository, rmaService);
        AceptarRMAUseCase aceptarRMA = new AceptarRMAUseCase(solicitudRepository, new ComponenteRepositoryEnMemoria(), rmaService);
        RechazarRMAUseCase rechazarRMA = new RechazarRMAUseCase(solicitudRepository, new ComponenteRepositoryEnMemoria(), rmaService);
        ConsultarRMAUseCase consultarRMA = new ConsultarRMAUseCase(solicitudRepository);

        Vendedor vendedor = Vendedor.crear(VENDEDOR_ID, "Distribuidor Oficial", "AUTORIZADO");
        Componente componente = Componente.crear(
                COMPONENTE_ID,
                "CPU",
                CategoriaComponente.CPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"),
                new Precio(new BigDecimal("100000"), "COP"),
                vendedor,
                GARANTIA_DIAS);
        UnidadInventario unidad = componente.agregarUnidad("v1", SERIAL, false);
        componente.reservarUnidad(unidad.getId());
        componente.venderUnidad(unidad.getId());
        componenteRepository.guardar(componente);

        Compra compra = Compra.realizar(COMPRA_ID, null, COMPRADOR_ID,
                List.of(new ItemCompra(unidad.getId(), COMPONENTE_ID, SERIAL, new Precio(new BigDecimal("100000"), "COP"))));
        compra.confirmar();
        compraRepository.guardar(compra);
    }

    private void aceptarRMA(String compraId, String rmaId, String numeroSerie) {
        solicitarRMA.ejecutar(new SolicitarRMARequest(
                rmaId, compraId, numeroSerie, COMPRADOR_ID, LocalDate.now()));
        aceptarRMA.ejecutar(rmaId, VENDEDOR_ID);
    }

    private void rechazarRMA(String compraId, String rmaId, String numeroSerie) {
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