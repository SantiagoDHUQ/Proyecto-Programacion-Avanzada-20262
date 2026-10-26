package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoComercial;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ComponenteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComponenteRepository componenteRepository;

    private String id(String prefijo) {
        return prefijo + "-" + UUID.randomUUID();
    }

    private void crearVendedor(String vendedorId, String tipo) throws Exception {
        mockMvc.perform(post("/api/vendedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"%s","nombre":"Vendedor %s","tipo":"%s"}
                                """.formatted(vendedorId, vendedorId, tipo)))
                .andExpect(status().isCreated());
    }

    private void crearComponente(String componenteId, String vendedorId, String categoria,
                                 String socket, String tipoMemoria, Integer garantia) throws Exception {
        String socketJson = socket == null ? "null" : "\"" + socket + "\"";
        String memoriaJson = tipoMemoria == null ? "null" : "\"" + tipoMemoria + "\"";
        String garantiaJson = garantia == null ? "null" : garantia.toString();

        mockMvc.perform(post("/api/componentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "%s",
                                  "nombre": "Pieza %s",
                                  "categoria": "%s",
                                  "socket": %s,
                                  "wattajeRequerido": 120,
                                  "tipoMemoria": %s,
                                  "moneda": "COP",
                                  "precio": 100000,
                                  "duracionGarantia": %s,
                                  "vendedorId": "%s"
                                }
                                """.formatted(componenteId, componenteId, categoria,
                                socketJson, memoriaJson, garantiaJson, vendedorId)))
                .andExpect(status().isCreated());
    }

    private void agregarUnidad(String componenteId, String vendedorId, String serial) throws Exception {
        mockMvc.perform(post("/api/componentes/{id}/unidades", componenteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s","numeroSerie":"%s"}
                                """.formatted(vendedorId, serial)))
                .andExpect(status().isOk());
    }

    @Test
    void crearComponenteAgregarUnidadYConsultarlo() throws Exception {
        String vendedorId = id("v");
        String componenteId = id("cpu");
        crearVendedor(vendedorId, "AUTORIZADO");
        crearComponente(componenteId, vendedorId, "CPU", "AM5", "DDR5", 365);

        mockMvc.perform(get("/api/componentes/{id}", componenteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(componenteId))
                .andExpect(jsonPath("$.categoria").value("CPU"))
                .andExpect(jsonPath("$.estadoComercial").value("PUBLICADO"))
                .andExpect(jsonPath("$.garantiaDias").value(365))
                .andExpect(jsonPath("$.stockDisponible").value(0))
                .andExpect(jsonPath("$.stockPreventa").value(0))
                .andExpect(jsonPath("$.unidades").isEmpty());

        agregarUnidad(componenteId, vendedorId, "SER-" + componenteId);

        mockMvc.perform(get("/api/componentes/{id}", componenteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockDisponible").value(1))
                .andExpect(jsonPath("$.unidades[0].numeroSerie").value("SER-" + componenteId))
                .andExpect(jsonPath("$.unidades[0].estado").value("DISPONIBLE"));

        UUID unidadId = componenteRepository.buscarPorId(componenteId).orElseThrow()
                .getUnidades().getFirst().getId();

        mockMvc.perform(get("/api/componentes/{id}/unidades/{unidadId}", componenteId, unidadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(unidadId.toString()))
                .andExpect(jsonPath("$.numeroSerie").value("SER-" + componenteId))
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));

        mockMvc.perform(get("/api/componentes/{id}/unidades/{unidadId}", componenteId, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void elSerialEsUnicoEnTodoElCatalogo() throws Exception {
        String vendedorId = id("v");
        String primero = id("comp");
        String segundo = id("comp");
        crearVendedor(vendedorId, "AUTORIZADO");
        crearComponente(primero, vendedorId, "RAM", null, "DDR5", 365);
        crearComponente(segundo, vendedorId, "RAM", null, "DDR5", 365);

        agregarUnidad(primero, vendedorId, "SER-GLOBAL");

        mockMvc.perform(post("/api/componentes/{id}/unidades", segundo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s","numeroSerie":"SER-GLOBAL"}
                                """.formatted(vendedorId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("número de serie")));
    }

    @Test
    void regla9SoloElVendedorDuenoPuedeAgregarUnidades() throws Exception {
        String dueno = id("dueno");
        String intruso = id("intruso");
        String componenteId = id("comp");
        crearVendedor(dueno, "AUTORIZADO");
        crearVendedor(intruso, "AUTORIZADO");
        crearComponente(componenteId, dueno, "PSU", null, null, 365);

        mockMvc.perform(post("/api/componentes/{id}/unidades", componenteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s","numeroSerie":"SER-INTRUSO"}
                                """.formatted(intruso)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("dueño")));

        Componente componente = componenteRepository.buscarPorId(componenteId).orElseThrow();
        assertEquals(0, componente.getUnidades().size());
    }

    @Test
    void unVendedorAutorizadoPuedePublicarPreventaYUnParticularNo() throws Exception {
        String autorizado = id("aut");
        String particular = id("part");
        String componenteAutorizado = id("comp");
        String componenteParticular = id("comp");
        crearVendedor(autorizado, "AUTORIZADO");
        crearVendedor(particular, "PARTICULAR");
        crearComponente(componenteAutorizado, autorizado, "GPU", "PCIe", "DDR5", 365);
        crearComponente(componenteParticular, particular, "GPU", "PCIe", "DDR5", null);

        LocalDate fecha = LocalDate.now().plusDays(10);
        mockMvc.perform(post("/api/componentes/{id}/preventas", componenteAutorizado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s","fechaEstimadaLlegada":"%s"}
                                """.formatted(autorizado, fecha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockPreventa").value(1))
                .andExpect(jsonPath("$.unidades[0].estado").value("PREVENTA"))
                .andExpect(jsonPath("$.unidades[0].fechaEstimadaLlegada").value(fecha.toString()));

        mockMvc.perform(post("/api/componentes/{id}/preventas", componenteParticular)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s","fechaEstimadaLlegada":"%s"}
                                """.formatted(particular, fecha)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("preventa")));
    }

    @Test
    void marcarAgotadoNoBorraLasUnidadesYEliminarEsLogico() throws Exception {
        String dueno = id("dueno");
        String otro = id("otro");
        String componenteId = id("comp");
        crearVendedor(dueno, "AUTORIZADO");
        crearVendedor(otro, "AUTORIZADO");
        crearComponente(componenteId, dueno, "STORAGE", null, null, 365);
        agregarUnidad(componenteId, dueno, "SER-STORAGE");

        mockMvc.perform(put("/api/componentes/{id}/agotado", componenteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s"}
                                """.formatted(dueno)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoComercial").value("AGOTADO"))
                .andExpect(jsonPath("$.stockDisponible").value(1))
                .andExpect(jsonPath("$.unidades[0].estado").value("DISPONIBLE"));

        mockMvc.perform(put("/api/componentes/{id}/publicar", componenteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s"}
                                """.formatted(otro)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/componentes/{id}/publicar", componenteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s"}
                                """.formatted(dueno)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoComercial").value("PUBLICADO"))
                .andExpect(jsonPath("$.stockDisponible").value(1));

        mockMvc.perform(delete("/api/componentes/{id}", componenteId)
                        .param("vendedorId", otro))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/componentes/{id}", componenteId)
                        .param("vendedorId", dueno))
                .andExpect(status().isNoContent());

        Componente componente = componenteRepository.buscarPorId(componenteId).orElseThrow();
        assertEquals(EstadoComercial.ELIMINADO, componente.getEstadoComercial());
        assertEquals(1, componente.getUnidades().size());
        assertEquals(EstadoUnidad.DISPONIBLE, componente.getUnidades().getFirst().getEstado());
        assertTrue(componente.estaEliminado());

        mockMvc.perform(get("/api/componentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(componenteId))));
    }

    @Test
    void noSePuedeCrearUnComponenteConUnVendedorInexistente() throws Exception {
        mockMvc.perform(post("/api/componentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id":"comp-sin-vendedor",
                                  "nombre":"Pieza",
                                  "categoria":"CPU",
                                  "socket":"AM5",
                                  "wattajeRequerido":120,
                                  "tipoMemoria":"DDR5",
                                  "moneda":"COP",
                                  "precio":100000,
                                  "duracionGarantia":365,
                                  "vendedorId":"no-existe"
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
