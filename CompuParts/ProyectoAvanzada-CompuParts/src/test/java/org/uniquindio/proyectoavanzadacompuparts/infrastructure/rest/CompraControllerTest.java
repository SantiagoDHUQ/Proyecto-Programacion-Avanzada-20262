package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.CancelarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConfirmarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RealizarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

@WebMvcTest(CompraController.class)
class CompraControllerTest {

    private static final UUID BUILD_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RealizarCompraUseCase realizarCompraUseCase;

    @MockitoBean
    private ConfirmarCompraUseCase confirmarCompraUseCase;

    @MockitoBean
    private CancelarCompraUseCase cancelarCompraUseCase;

    @MockitoBean
    private ConsultarCompraUseCase consultarCompraUseCase;

    @Test
    void debeCrearUnaCompraCuandoLosDatosSonValidos() throws Exception {
        Compra compra = compraPendiente();
        when(realizarCompraUseCase.ejecutar(any(), any())).thenReturn(compra);

        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"buildId":"%s","compradorId":"comprador-1"}
                                """.formatted(BUILD_ID)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/compras/compra-1")))
                .andExpect(jsonPath("$.id").value("compra-1"))
                .andExpect(jsonPath("$.buildId").value(BUILD_ID.toString()))
                .andExpect(jsonPath("$.compradorId").value("comprador-1"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.total").value(100000))
                .andExpect(jsonPath("$.moneda").value("COP"));

        verify(realizarCompraUseCase).ejecutar(BUILD_ID, "comprador-1");
    }

    @Test
    void debeRetornar400CuandoFaltaElBuildId() throws Exception {
        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compradorId":"comprador-1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("buildId")));

        verifyNoInteractions(realizarCompraUseCase);
    }

    @Test
    void debeConsultarUnaCompraExistente() throws Exception {
        when(consultarCompraUseCase.ejecutar("compra-1")).thenReturn(compraPendiente());

        mockMvc.perform(get("/api/compras/{id}", "compra-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("compra-1"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void debeRetornar404CuandoLaCompraNoExiste() throws Exception {
        when(consultarCompraUseCase.ejecutar("compra-inexistente"))
                .thenThrow(new RecursoNoEncontradoException("No existe la compra"));

        mockMvc.perform(get("/api/compras/{id}", "compra-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la compra"));
    }

    @Test
    void debeConfirmarUnaCompraPendiente() throws Exception {
        Compra compra = compraPendiente();
        compra.confirmar();
        when(confirmarCompraUseCase.ejecutar("compra-1")).thenReturn(compra);

        mockMvc.perform(post("/api/compras/{id}/confirmar", "compra-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));

        verify(confirmarCompraUseCase).ejecutar("compra-1");
    }

    @Test
    void debeCancelarUnaCompraPendiente() throws Exception {
        Compra compra = compraPendiente();
        compra.cancelar();
        when(cancelarCompraUseCase.ejecutar("compra-1")).thenReturn(compra);

        mockMvc.perform(post("/api/compras/{id}/cancelar", "compra-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        verify(cancelarCompraUseCase).ejecutar("compra-1");
    }

    private Compra compraPendiente() {
        Precio precio = new Precio(new BigDecimal("100000"), "COP");
        ItemCompra item = new ItemCompra(UUID.randomUUID(), "comp-1", "SER-1", precio);
        return Compra.realizar("compra-1", BUILD_ID, "comprador-1", List.of(item));
    }
}
