package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RMAApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VendedorRepository vendedorRepository;

    @Autowired
    private ComponenteRepository componenteRepository;

    @Autowired
    private CompraRepository compraRepository;

    private String id(String prefijo) {
        return prefijo + "-" + UUID.randomUUID();
    }

    private record Escenario(String vendedorId, String componenteId, String compraId,
                             String serial, String compradorId) {
    }

    private Escenario crearCompraVendida(String prefijo) {
        String vendedorId = prefijo + "-vendedor";
        String componenteId = prefijo + "-componente";
        String compraId = prefijo + "-compra";
        String serial = prefijo + "-SER";
        String compradorId = prefijo + "-comprador";

        Vendedor vendedor = Vendedor.crear(vendedorId, "Distribuidor Oficial", "AUTORIZADO");
        vendedorRepository.guardar(vendedor);

        Precio precio = new Precio(new BigDecimal("100000"), "COP");
        Componente componente = Componente.crear(
                componenteId,
                "CPU",
                CategoriaComponente.CPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"),
                precio,
                vendedor,
                365);
        UnidadInventario unidad = componente.agregarUnidad(serial);
        componente.reservarUnidad(unidad.getId());
        componente.venderUnidad(unidad.getId());
        componenteRepository.guardar(componente);

        Compra compra = Compra.realizar(compraId, null, compradorId,
                List.of(new ItemCompra(unidad.getId(), componenteId, serial, precio)));
        compra.confirmar();
        compraRepository.guardar(compra);

        return new Escenario(vendedorId, componenteId, compraId, serial, compradorId);
    }

    private String solicitarRMA(Escenario escenario, String rmaId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/rma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id":"%s",
                                  "compraId":"%s",
                                  "numeroSerie":"%s",
                                  "compradorId":"%s"
                                }
                                """.formatted(rmaId, escenario.compraId(), escenario.serial(),
                                escenario.compradorId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.numeroSerie").value(escenario.serial()))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    @Test
    void elCompradorSolicitaYElVendedorDuenoAceptaLaRMA() throws Exception {
        Escenario escenario = crearCompraVendida(id("rma"));
        String rmaId = solicitarRMA(escenario, id("solicitud"));

        mockMvc.perform(get("/api/rma/{id}", rmaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rmaId))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        mockMvc.perform(post("/api/rma/{id}/aceptar", rmaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s"}
                                """.formatted(escenario.vendedorId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));

        Componente componente = componenteRepository.buscarPorId(escenario.componenteId())
                .orElseThrow();
        assertEquals(EstadoUnidad.EN_RMA, componente.getUnidades().getFirst().getEstado());
    }

    @Test
    void unVendedorAjenoNoPuedeResolverYElDuenoPuedeRechazar() throws Exception {
        Escenario escenario = crearCompraVendida(id("rma"));
        String rmaId = solicitarRMA(escenario, id("solicitud"));

        mockMvc.perform(post("/api/rma/{id}/aceptar", rmaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"otro-vendedor"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Solo el vendedor dueño puede operar este componente"));

        mockMvc.perform(post("/api/rma/{id}/rechazar", rmaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s","motivo":"Daño no cubierto"}
                                """.formatted(escenario.vendedorId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"))
                .andExpect(jsonPath("$.motivoRechazo").value("Daño no cubierto"));

        Componente componente = componenteRepository.buscarPorId(escenario.componenteId())
                .orElseThrow();
        assertEquals(EstadoUnidad.VENDIDA, componente.getUnidades().getFirst().getEstado());
    }

    @Test
    void elCompradorReembolsaLaCompraTrasLaRMAAceptadaYLaUnidadSeDescarta() throws Exception {
        Escenario escenario = crearCompraVendida(id("reembolso"));
        String rmaId = solicitarRMA(escenario, id("solicitud"));

        mockMvc.perform(post("/api/rma/{id}/aceptar", rmaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s"}
                                """.formatted(escenario.vendedorId())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/compras/{id}/reembolsar", escenario.compraId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compradorId":"%s","motivo":"Falla de fábrica"}
                                """.formatted(escenario.compradorId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REEMBOLSADA"));

        Componente componente = componenteRepository.buscarPorId(escenario.componenteId())
                .orElseThrow();
        assertEquals(EstadoUnidad.DESCARTADA, componente.getUnidades().getFirst().getEstado());
    }

    @Test
    void noSePuedeReembolsarUnaCompraConRMAPendiente() throws Exception {
        Escenario escenario = crearCompraVendida(id("reembolso-pendiente"));
        solicitarRMA(escenario, id("solicitud"));
        assertEquals(EstadoUnidad.EN_RMA,
                componenteRepository.buscarPorId(escenario.componenteId())
                        .orElseThrow().getUnidades().getFirst().getEstado());

        mockMvc.perform(post("/api/compras/{id}/reembolsar", escenario.compraId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compradorId":"%s","motivo":"Falla de fábrica"}
                                """.formatted(escenario.compradorId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "La unidad " + escenario.serial() + " tiene una SolicitudRMA pendiente"));

        assertEquals(EstadoUnidad.EN_RMA, componenteRepository.buscarPorId(escenario.componenteId())
                .orElseThrow().getUnidades().getFirst().getEstado());
        assertEquals(EstadoCompra.COMPLETADA,
                compraRepository.buscarPorId(escenario.compraId()).orElseThrow().getEstado());
    }

    @Test
    void consultarUnaRMAnoExistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/rma/{id}", "rma-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe una SolicitudRMA con ese identificador"));
    }

    @Test
    void solicitarRMASinNumeroSerieDevuelve400() throws Exception {
        mockMvc.perform(post("/api/rma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"rma-1","compraId":"compra-1","compradorId":"comprador-1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "numeroSerie: El número de serie es necesario"));
    }
}
