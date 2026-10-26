package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class VendedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String id(String prefijo) {
        return prefijo + "-" + UUID.randomUUID();
    }

    private void registrar(String vendedorId, String nombre, String tipo) throws Exception {
        mockMvc.perform(post("/api/vendedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"%s","nombre":"%s","tipo":"%s"}
                                """.formatted(vendedorId, nombre, tipo)))
                .andExpect(status().isCreated());
    }

    @Test
    void registrarYConsultarUnVendedor() throws Exception {
        String vendedorId = id("v");
        registrar(vendedorId, "Distribuidor Oficial", "AUTORIZADO");

        mockMvc.perform(get("/api/vendedores/{id}", vendedorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vendedorId))
                .andExpect(jsonPath("$.nombre").value("Distribuidor Oficial"))
                .andExpect(jsonPath("$.tipo").value("AUTORIZADO"));
    }

    @Test
    void noSePuedeRegistrarDosVecesElMismoVendedor() throws Exception {
        String vendedorId = id("v");
        registrar(vendedorId, "Vendedor Uno", "PARTICULAR");

        mockMvc.perform(post("/api/vendedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"%s","nombre":"Vendedor Dos","tipo":"PARTICULAR"}
                                """.formatted(vendedorId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("Ya existe")));
    }

    @Test
    void listarVendedoresIncluyeLosRegistrados() throws Exception {
        String vendedorId = id("v");
        registrar(vendedorId, "Vendedor Listado", "AUTORIZADO");

        mockMvc.perform(get("/api/vendedores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(vendedorId)));
    }

    @Test
    void consultarUnVendedorInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/vendedores/{id}", id("sin-vendedor")))
                .andExpect(status().isNotFound());
    }
}
