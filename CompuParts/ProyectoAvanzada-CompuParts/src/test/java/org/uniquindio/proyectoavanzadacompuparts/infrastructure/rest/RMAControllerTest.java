package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.AceptarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ListarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RechazarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.SolicitarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.time.LocalDate;
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

@WebMvcTest(RMAController.class)
class RMAControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolicitarRMAUseCase solicitarRMAUseCase;

    @MockitoBean
    private AceptarRMAUseCase aceptarRMAUseCase;

    @MockitoBean
    private RechazarRMAUseCase rechazarRMAUseCase;

    @MockitoBean
    private ConsultarRMAUseCase consultarRMAUseCase;

    @MockitoBean
    private ListarRMAUseCase listarRMAUseCase;

    @Test
    void debeSolicitarUnaRMAValida() throws Exception {
        SolicitudRMA solicitud = solicitudPendiente();
        when(solicitarRMAUseCase.ejecutar(any())).thenReturn(solicitud);

        mockMvc.perform(post("/api/rma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id":"rma-1",
                                  "compraId":"compra-1",
                                  "numeroSerie":"SER-1",
                                  "compradorId":"comprador-1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/rma/rma-1")))
                .andExpect(jsonPath("$.id").value("rma-1"))
                .andExpect(jsonPath("$.numeroSerie").value("SER-1"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        verify(solicitarRMAUseCase).ejecutar(any());
    }

    @Test
    void debeRetornar400CuandoFaltaElNumeroDeSerie() throws Exception {
        mockMvc.perform(post("/api/rma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"rma-1","compraId":"compra-1","compradorId":"comprador-1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("numeroSerie")));

        verifyNoInteractions(solicitarRMAUseCase);
    }

    @Test
    void debeConsultarUnaRMAExistente() throws Exception {
        when(consultarRMAUseCase.ejecutar("rma-1")).thenReturn(solicitudPendiente());

        mockMvc.perform(get("/api/rma/{id}", "rma-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("rma-1"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void debeRetornar404CuandoLaRMAnoExiste() throws Exception {
        when(consultarRMAUseCase.ejecutar("rma-inexistente"))
                .thenThrow(new RecursoNoEncontradoException("No existe la solicitud RMA"));

        mockMvc.perform(get("/api/rma/{id}", "rma-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la solicitud RMA"));
    }

    @Test
    void debeListarLasSolicitudesRMA() throws Exception {
        when(listarRMAUseCase.ejecutar()).thenReturn(List.of(solicitudPendiente()));

        mockMvc.perform(get("/api/rma"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("rma-1"));
    }

    @Test
    void elVendedorDuenoDebeAceptarLaRMA() throws Exception {
        SolicitudRMA solicitud = solicitudPendiente();
        solicitud.aceptar();
        when(aceptarRMAUseCase.ejecutar("rma-1", "v-1")).thenReturn(solicitud);

        mockMvc.perform(post("/api/rma/{id}/aceptar", "rma-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"v-1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));

        verify(aceptarRMAUseCase).ejecutar("rma-1", "v-1");
    }

    @Test
    void debeRetornar400CuandoUnVendedorAjenoIntentaAceptar() throws Exception {
        when(aceptarRMAUseCase.ejecutar("rma-1", "otro"))
                .thenThrow(new ReglaDominioException("Solo el vendedor dueño puede operar este componente"));

        mockMvc.perform(post("/api/rma/{id}/aceptar", "rma-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"otro"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "Solo el vendedor dueño puede operar este componente"));
    }

    @Test
    void elVendedorDuenoDebeRechazarLaRMAConMotivo() throws Exception {
        SolicitudRMA solicitud = solicitudPendiente();
        solicitud.rechazar("Daño no cubierto");
        when(rechazarRMAUseCase.ejecutar("rma-1", "v-1", "Daño no cubierto"))
                .thenReturn(solicitud);

        mockMvc.perform(post("/api/rma/{id}/rechazar", "rma-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"v-1","motivo":"Daño no cubierto"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"))
                .andExpect(jsonPath("$.motivoRechazo").value("Daño no cubierto"));

        verify(rechazarRMAUseCase).ejecutar("rma-1", "v-1", "Daño no cubierto");
    }

    @Test
    void debeRetornar400CuandoElRechazoNoTieneMotivo() throws Exception {
        mockMvc.perform(post("/api/rma/{id}/rechazar", "rma-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"v-1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("motivo")));

        verifyNoInteractions(rechazarRMAUseCase);
    }

    private SolicitudRMA solicitudPendiente() {
        return SolicitudRMA.crear(
                "rma-1",
                "comp-1",
                "compra-1",
                "SER-1",
                "comprador-1",
                LocalDate.now().minusDays(10),
                365,
                LocalDate.now()
        );
    }
}
