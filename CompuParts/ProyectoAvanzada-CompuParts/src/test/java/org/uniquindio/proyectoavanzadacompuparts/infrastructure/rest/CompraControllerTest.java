package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComponenteRepository componenteRepository;

    private String id(String prefijo) {
        return prefijo + "-" + UUID.randomUUID();
    }

    private void crearVendedor(String vendedorId) throws Exception {
        mockMvc.perform(post("/api/vendedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"%s","nombre":"Distribuidor","tipo":"AUTORIZADO"}
                                """.formatted(vendedorId)))
                .andExpect(status().isCreated());
    }

    private void crearComponente(String componenteId, String vendedorId, String categoria,
                                 String socket, int wattaje, String tipoMemoria, String serial) throws Exception {
        String socketJson = socket == null ? "null" : "\"" + socket + "\"";
        String memoriaJson = tipoMemoria == null ? "null" : "\"" + tipoMemoria + "\"";
        mockMvc.perform(post("/api/componentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id":"%s","nombre":"Pieza","categoria":"%s",
                                  "socket":%s,"wattajeRequerido":%d,"tipoMemoria":%s,
                                  "moneda":"COP","precio":100000,"duracionGarantia":365,
                                  "vendedorId":"%s"
                                }
                                """.formatted(componenteId, categoria, socketJson, wattaje, memoriaJson, vendedorId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/componentes/{id}/unidades", componenteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s","numeroSerie":"%s"}
                                """.formatted(vendedorId, serial)))
                .andExpect(status().isOk());
    }

    private String crearBuild(String vendedorId, String prefijo) throws Exception {
        String cpu = prefijo + "-cpu";
        String board = prefijo + "-mb";
        String psu = prefijo + "-psu";

        crearComponente(cpu, vendedorId, "CPU", "AM5", 120, "DDR5", prefijo + "-SER-CPU");
        crearComponente(board, vendedorId, "MOTHERBOARD", "AM5", 80, "DDR5", prefijo + "-SER-MB");
        crearComponente(psu, vendedorId, "PSU", null, 500, null, prefijo + "-SER-PSU");

        MvcResult buildResult = mockMvc.perform(post("/api/builds"))
                .andExpect(status().isCreated())
                .andReturn();
        String buildId = JsonPath.read(buildResult.getResponse().getContentAsString(), "$.buildId");

        for (String componenteId : List.of(cpu, board, psu)) {
            mockMvc.perform(post("/api/builds/{buildId}/componentes", buildId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"componenteId":"%s"}
                                    """.formatted(componenteId)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/builds/{buildId}/listo-para-compra", buildId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("LISTO_PARA_COMPRA"));

        return buildId;
    }

    private String realizarCompra(String buildId, String compradorId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"buildId":"%s","compradorId":"%s"}
                                """.formatted(buildId, compradorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.items.length()").value(3))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private Componente componente(String componenteId) {
        return componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new AssertionError("No existe el componente " + componenteId));
    }

    @Test
    void unaCompraPendienteReservaStockYAlCancelarseLoLibera() throws Exception {
        String vendedorId = id("v");
        String prefijo = id("build");
        crearVendedor(vendedorId);
        String buildId = crearBuild(vendedorId, prefijo);

        String compraId = realizarCompra(buildId, "comprador-1");

        for (String sufijo : List.of("cpu", "mb", "psu")) {
            Componente componente = componente(prefijo + "-" + sufijo);
            assertEquals(0, componente.stockDisponible());
            assertEquals("RESERVADA", componente.getUnidades().getFirst().getEstado().name());
        }

        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"buildId":"%s","compradorId":"comprador-2"}
                                """.formatted(buildId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/compras/{id}/cancelar", compraId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        for (String sufijo : List.of("cpu", "mb", "psu")) {
            Componente componente = componente(prefijo + "-" + sufijo);
            assertEquals(1, componente.stockDisponible());
            assertEquals("DISPONIBLE", componente.getUnidades().getFirst().getEstado().name());
        }

        String segundaCompra = realizarCompra(buildId, "comprador-2");
        mockMvc.perform(post("/api/compras/{id}/confirmar", segundaCompra))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));

        for (String sufijo : List.of("cpu", "mb", "psu")) {
            Componente componente = componente(prefijo + "-" + sufijo);
            assertEquals(0, componente.stockDisponible());
            assertEquals("VENDIDA", componente.getUnidades().getFirst().getEstado().name());
        }

        mockMvc.perform(get("/api/builds/{id}", buildId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPRADO"));

        mockMvc.perform(post("/api/compras/{id}/cancelar", segundaCompra))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unBuildQueNoEstaListoNoSePuedeComprar() throws Exception {
        String vendedorId = id("v");
        String prefijo = id("build");
        crearVendedor(vendedorId);

        String cpu = prefijo + "-cpu";
        String board = prefijo + "-mb";
        String psu = prefijo + "-psu";
        crearComponente(cpu, vendedorId, "CPU", "AM5", 120, "DDR5", prefijo + "-SER-CPU");
        crearComponente(board, vendedorId, "MOTHERBOARD", "AM5", 80, "DDR5", prefijo + "-SER-MB");
        crearComponente(psu, vendedorId, "PSU", null, 500, null, prefijo + "-SER-PSU");

        MvcResult buildResult = mockMvc.perform(post("/api/builds"))
                .andExpect(status().isCreated())
                .andReturn();
        String buildId = JsonPath.read(buildResult.getResponse().getContentAsString(), "$.buildId");
        for (String componenteId : List.of(cpu, board, psu)) {
            mockMvc.perform(post("/api/builds/{buildId}/componentes", buildId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"componenteId":"%s"}
                                    """.formatted(componenteId)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"buildId":"%s","compradorId":"comprador-1"}
                                """.formatted(buildId)))
                .andExpect(status().isBadRequest());
    }
}
