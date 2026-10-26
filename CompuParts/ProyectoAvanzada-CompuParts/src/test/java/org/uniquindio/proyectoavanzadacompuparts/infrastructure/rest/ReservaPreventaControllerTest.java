package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.CancelarReservaPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ComprarReservaPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarReservaPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ReservarPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaPreventaController.class)
class ReservaPreventaControllerTest {

    private static final String COMPONENTE_ID = "comp-1";
    private static final UUID UNIDAD_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String COMPRADOR_ID = "comprador-1";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservarPreventaUseCase reservarPreventaUseCase;

    @MockitoBean
    private CancelarReservaPreventaUseCase cancelarReservaPreventaUseCase;

    @MockitoBean
    private ConsultarReservaPreventaUseCase consultarReservaPreventaUseCase;

    @MockitoBean
    private ComprarReservaPreventaUseCase comprarReservaPreventaUseCase;

    @Test
    void debeReservarUnaUnidadEnPreventa() throws Exception {
        ReservaPreventa reserva = reservaActiva();
        when(reservarPreventaUseCase.ejecutar(COMPONENTE_ID, UNIDAD_ID, COMPRADOR_ID))
                .thenReturn(reserva);

        mockMvc.perform(post("/api/componentes/{componenteId}/unidades/{unidadId}/reservas-preventa",
                        COMPONENTE_ID, UNIDAD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compradorId":"comprador-1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/reservas-preventa/" + reserva.getId())))
                .andExpect(jsonPath("$.id").value(reserva.getId().toString()))
                .andExpect(jsonPath("$.componenteId").value(COMPONENTE_ID))
                .andExpect(jsonPath("$.unidadId").value(UNIDAD_ID.toString()))
                .andExpect(jsonPath("$.compradorId").value(COMPRADOR_ID))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));

        verify(reservarPreventaUseCase).ejecutar(COMPONENTE_ID, UNIDAD_ID, COMPRADOR_ID);
    }

    @Test
    void debeRetornar400CuandoFaltaElComprador() throws Exception {
        mockMvc.perform(post("/api/componentes/{componenteId}/unidades/{unidadId}/reservas-preventa",
                        COMPONENTE_ID, UNIDAD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("compradorId")));

        verifyNoInteractions(reservarPreventaUseCase);
    }

    @Test
    void debeConsultarUnaReservaExistente() throws Exception {
        ReservaPreventa reserva = reservaActiva();
        when(consultarReservaPreventaUseCase.ejecutar(reserva.getId())).thenReturn(reserva);

        mockMvc.perform(get("/api/reservas-preventa/{id}", reserva.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reserva.getId().toString()))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    void debeRetornar404CuandoLaReservaNoExiste() throws Exception {
        UUID reservaId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(consultarReservaPreventaUseCase.ejecutar(reservaId))
                .thenThrow(new RecursoNoEncontradoException("No existe la reserva"));

        mockMvc.perform(get("/api/reservas-preventa/{id}", reservaId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la reserva"));
    }

    @Test
    void debeCancelarLaReservaDelComprador() throws Exception {
        ReservaPreventa reserva = reservaActiva();
        reserva.cancelar();
        when(cancelarReservaPreventaUseCase.ejecutar(reserva.getId(), COMPRADOR_ID))
                .thenReturn(reserva);

        mockMvc.perform(delete("/api/reservas-preventa/{id}", reserva.getId())
                        .param("compradorId", COMPRADOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        verify(cancelarReservaPreventaUseCase).ejecutar(reserva.getId(), COMPRADOR_ID);
    }

    @Test
    void debeComprarUnaReservaYaRecibida() throws Exception {
        ReservaPreventa reserva = reservaActiva();
        Compra compra = compraDirecta();
        when(comprarReservaPreventaUseCase.ejecutar(reserva.getId(), COMPRADOR_ID)).thenReturn(compra);

        mockMvc.perform(post("/api/reservas-preventa/{id}/compra", reserva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compradorId":"comprador-1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/compras/compra-preventa")))
                .andExpect(jsonPath("$.id").value("compra-preventa"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.items.length()").value(1));

        verify(comprarReservaPreventaUseCase).ejecutar(reserva.getId(), COMPRADOR_ID);
    }

    @Test
    void debeRetornar400CuandoLaPreventaAunNoHaLlegado() throws Exception {
        ReservaPreventa reserva = reservaActiva();
        when(comprarReservaPreventaUseCase.ejecutar(reserva.getId(), COMPRADOR_ID))
                .thenThrow(new ReglaDominioException("La unidad aún no ha llegado"));

        mockMvc.perform(post("/api/reservas-preventa/{id}/compra", reserva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compradorId":"comprador-1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La unidad aún no ha llegado"));
    }

    private ReservaPreventa reservaActiva() {
        return ReservaPreventa.crear(COMPONENTE_ID, UNIDAD_ID, COMPRADOR_ID);
    }

    private Compra compraDirecta() {
        Precio precio = new Precio(new BigDecimal("100000"), "COP");
        ItemCompra item = new ItemCompra(UNIDAD_ID, COMPONENTE_ID, "SER-1", precio);
        return Compra.realizar("compra-preventa", null, COMPRADOR_ID, List.of(item));
    }
}
