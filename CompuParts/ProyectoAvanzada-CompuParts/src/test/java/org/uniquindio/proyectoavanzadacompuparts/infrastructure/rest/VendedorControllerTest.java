package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarVendedorUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ListarVendedoresUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RegistrarVendedorUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VendedorController.class)
class VendedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarVendedorUseCase registrarVendedorUseCase;

    @MockitoBean
    private ConsultarVendedorUseCase consultarVendedorUseCase;

    @MockitoBean
    private ListarVendedoresUseCase listarVendedoresUseCase;

    @Test
    void debeRegistrarVendedorCuandoLosDatosSonValidos() throws Exception {
        Vendedor vendedor = Vendedor.crear("v-1", "Distribuidor Oficial", "AUTORIZADO");
        when(registrarVendedorUseCase.ejecutar(any())).thenReturn(vendedor);

        mockMvc.perform(post("/api/vendedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"v-1","nombre":"Distribuidor Oficial","tipo":"AUTORIZADO"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/vendedores/v-1")))
                .andExpect(jsonPath("$.id").value("v-1"))
                .andExpect(jsonPath("$.nombre").value("Distribuidor Oficial"))
                .andExpect(jsonPath("$.tipo").value("AUTORIZADO"));

        verify(registrarVendedorUseCase).ejecutar(any());
    }

    @Test
    void debeRetornar400CuandoFaltaElNombre() throws Exception {
        mockMvc.perform(post("/api/vendedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"v-1","tipo":"AUTORIZADO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("nombre")));

        verifyNoInteractions(registrarVendedorUseCase);
    }

    @Test
    void debeConsultarUnVendedorExistente() throws Exception {
        Vendedor vendedor = Vendedor.crear("v-1", "Distribuidor Oficial", "AUTORIZADO");
        when(consultarVendedorUseCase.ejecutar("v-1")).thenReturn(vendedor);

        mockMvc.perform(get("/api/vendedores/{id}", "v-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("v-1"))
                .andExpect(jsonPath("$.nombre").value("Distribuidor Oficial"))
                .andExpect(jsonPath("$.tipo").value("AUTORIZADO"));
    }

    @Test
    void debeRetornar404CuandoElVendedorNoExiste() throws Exception {
        when(consultarVendedorUseCase.ejecutar("v-inexistente"))
                .thenThrow(new RecursoNoEncontradoException("No existe el vendedor"));

        mockMvc.perform(get("/api/vendedores/{id}", "v-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el vendedor"));
    }

    @Test
    void debeListarLosVendedores() throws Exception {
        Vendedor vendedor = Vendedor.crear("v-1", "Distribuidor Oficial", "AUTORIZADO");
        when(listarVendedoresUseCase.ejecutar()).thenReturn(List.of(vendedor));

        mockMvc.perform(get("/api/vendedores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("v-1"));
    }
}
