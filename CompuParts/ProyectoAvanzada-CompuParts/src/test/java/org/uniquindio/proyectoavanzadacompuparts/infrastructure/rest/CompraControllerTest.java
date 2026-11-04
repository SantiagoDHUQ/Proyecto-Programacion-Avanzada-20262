package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.CancelarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConfirmarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RealizarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ReembolsarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompraController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CompraControllerTest {

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

    @MockitoBean
    private ReembolsarCompraUseCase reembolsarCompraUseCase;

    @Test
    void realizarCompraExitosaRetorna201Created() throws Exception {
        // Arrange: Preparar datos válidos y compra esperada
        String compradorId = "comprador-1";
        Precio precio = new Precio(new BigDecimal("100000"), "COP");
        ItemCompra item = new ItemCompra(UUID.randomUUID(), "comp-1", "SER-1", precio);
        Compra compraSimulada = Compra.realizar("compra-1", BUILD_ID, compradorId, List.of(item));

        when(realizarCompraUseCase.ejecutar(eq(BUILD_ID), eq(compradorId))).thenReturn(compraSimulada);

        String jsonValido = """
                {
                    "buildId": "%s",
                    "compradorId": "%s"
                }
                """.formatted(BUILD_ID, compradorId);

        // Act & Assert: Petición HTTP simulada y verificación de respuesta 201 Created
        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/compras/compra-1")))
                .andExpect(jsonPath("$.id").value("compra-1"))
                .andExpect(jsonPath("$.buildId").value(BUILD_ID.toString()))
                .andExpect(jsonPath("$.compradorId").value(compradorId))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.total").value(100000))
                .andExpect(jsonPath("$.moneda").value("COP"));

        verify(realizarCompraUseCase).ejecutar(BUILD_ID, compradorId);
    }

    @Test
    void realizarCompraSinDatoObligatorioRetorna400BadRequest() throws Exception {
        // Arrange: JSON sin el campo obligatorio 'buildId'
        String jsonInvalido = """
                {
                    "compradorId": "comprador-1"
                }
                """;

        // Act & Assert: Petición con validación fallida (400 Bad Request)
        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("buildId")));

        // Verificar que el caso de uso NUNCA se ejecutó
        verify(realizarCompraUseCase, never()).ejecutar(any(), any());
    }

    @Test
    void consultarCompraConIdInexistenteRetorna404NotFound() throws Exception {
        // Arrange: ID inexistente que dispara la excepción de recurso no encontrado
        String idInexistente = "compra-no-existe";
        when(consultarCompraUseCase.ejecutar(idInexistente))
                .thenThrow(new RecursoNoEncontradoException("No existe la compra con ID: " + idInexistente));

        // Act & Assert: Verificación de respuesta 404 Not Found
        mockMvc.perform(get("/api/compras/{id}", idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la compra con ID: " + idInexistente));

        // Verificar que las acciones subsecuentes nunca prosiguieron
        verify(realizarCompraUseCase, never()).ejecutar(any(), any());
        verify(confirmarCompraUseCase, never()).ejecutar(any());
    }
}
