package org.uniquindio.proyectoavanzadacompuparts.infrastructure.security;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void peticionSinTokenAEndpointProtegidoDevuelve401Unauthorized() throws Exception {
        // Intentar registrar un componente sin header Authorization
        mockMvc.perform(post("/api/componentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "gpu-test-401",
                                  "nombre": "GeForce RTX 4070",
                                  "categoria": "GPU",
                                  "wattajeRequerido": 200,
                                  "moneda": "COP",
                                  "precio": 3200000,
                                  "duracionGarantia": 36,
                                  "vendedorId": "v1"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").value("Acceso no autorizado: Token no proporcionado o invalido"));
    }

    @Test
    void peticionConRolEquivocadoDevuelve403Forbidden() throws Exception {
        // 1. Iniciar sesión con el usuario comprador de data.sql
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "comprador@compuparts.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("COMPRADOR"))
                .andReturn();

        // 2. Capturar el token JWT
        String tokenComprador = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");

        // 3. Intentar acceder con token COMPRADOR a la ruta restringida a VENDEDOR (POST /api/componentes)
        mockMvc.perform(post("/api/componentes")
                        .header("Authorization", "Bearer " + tokenComprador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "gpu-test-403",
                                  "nombre": "GeForce RTX 4070",
                                  "categoria": "GPU",
                                  "wattajeRequerido": 200,
                                  "moneda": "COP",
                                  "precio": 3200000,
                                  "duracionGarantia": 36,
                                  "vendedorId": "v1"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensaje").value("Acceso denegado: No cuenta con el rol requerido para este recurso"));
    }

    @Test
    void peticionConRolCorrectoPermiteAccesoYDevuelve201Created() throws Exception {
        // 1. Iniciar sesión con el usuario vendedor de data.sql
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "vendedor@compuparts.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("VENDEDOR"))
                .andReturn();

        // 2. Capturar el token JWT
        String tokenVendedor = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");

        // 3. Crear componente usando el token de VENDEDOR en la ruta permitida
        mockMvc.perform(post("/api/componentes")
                        .header("Authorization", "Bearer " + tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "cpu-test-201",
                                  "nombre": "Intel Core i5-13400",
                                  "categoria": "CPU",
                                  "socket": "LGA1700",
                                  "wattajeRequerido": 65,
                                  "tipoMemoria": "DDR5",
                                  "moneda": "COP",
                                  "precio": 950000,
                                  "duracionGarantia": 30,
                                  "vendedorId": "v1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("cpu-test-201"))
                .andExpect(jsonPath("$.nombre").value("Intel Core i5-13400"));
    }
}

