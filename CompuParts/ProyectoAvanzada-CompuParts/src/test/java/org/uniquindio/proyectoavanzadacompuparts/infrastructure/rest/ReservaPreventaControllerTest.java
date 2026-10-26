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
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoUnidad;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservaPreventaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComponenteRepository componenteRepository;

    @Autowired
    private ReservaPreventaRepository reservaPreventaRepository;

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

    private void crearComponente(String componenteId, String vendedorId) throws Exception {
        mockMvc.perform(post("/api/componentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id":"%s","nombre":"GPU","categoria":"GPU",
                                  "socket":"PCIe","wattajeRequerido":250,"tipoMemoria":"DDR5",
                                  "moneda":"COP","precio":200000,"duracionGarantia":365,
                                  "vendedorId":"%s"
                                }
                                """.formatted(componenteId, vendedorId)))
                .andExpect(status().isCreated());
    }

    private UUID publicarPreventa(String componenteId, String vendedorId) throws Exception {
        mockMvc.perform(post("/api/componentes/{id}/preventas", componenteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s","fechaEstimadaLlegada":"%s"}
                                """.formatted(vendedorId, LocalDate.now().plusDays(10))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockPreventa").value(1));

        Componente componente = componenteRepository.buscarPorId(componenteId).orElseThrow();
        return componente.getUnidades().getFirst().getId();
    }

    private String reservar(String componenteId, UUID unidadId, String compradorId) throws Exception {
        MvcResult result = mockMvc.perform(post(
                                "/api/componentes/{componenteId}/unidades/{unidadId}/reservas-preventa",
                                componenteId, unidadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compradorId":"%s"}
                                """.formatted(compradorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private void recibir(String componenteId, UUID unidadId, String vendedorId, String serial) throws Exception {
        mockMvc.perform(post("/api/componentes/{id}/unidades/{unidadId}/recibir", componenteId, unidadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s","numeroSerie":"%s"}
                                """.formatted(vendedorId, serial)))
                .andExpect(status().isOk());
    }

    @Test
    void reservaRecibeConvierteEnCompraYLaCancelaSinPerderLaReserva() throws Exception {
        String vendedorId = id("v");
        String componenteId = id("gpu");
        String compradorId = id("comprador");
        String otroComprador = id("comprador");
        crearVendedor(vendedorId);
        crearComponente(componenteId, vendedorId);
        UUID unidadId = publicarPreventa(componenteId, vendedorId);

        String reservaId = reservar(componenteId, unidadId, compradorId);

        mockMvc.perform(post("/api/componentes/{componenteId}/unidades/{unidadId}/reservas-preventa",
                                componenteId, unidadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compradorId":"%s"}
                                """.formatted(otroComprador)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/reservas-preventa/{id}/compra", reservaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compradorId":"%s"}
                                """.formatted(compradorId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("aún no ha llegado")));

        recibir(componenteId, unidadId, vendedorId, "SER-PREVENTA");

        mockMvc.perform(get("/api/componentes/{id}", componenteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockDisponible").value(0))
                .andExpect(jsonPath("$.unidades[0].estado").value("RESERVADA"))
                .andExpect(jsonPath("$.unidades[0].numeroSerie").value("SER-PREVENTA"));

        mockMvc.perform(post("/api/reservas-preventa/{id}/compra", reservaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compradorId":"%s"}
                                """.formatted(otroComprador)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("Solo el comprador")));

        MvcResult compraResult = mockMvc.perform(post("/api/reservas-preventa/{id}/compra", reservaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compradorId":"%s"}
                                """.formatted(compradorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andReturn();
        String compraId = JsonPath.read(compraResult.getResponse().getContentAsString(), "$.id");

        ReservaPreventa enCompra = reservaPreventaRepository.buscarPorId(UUID.fromString(reservaId)).orElseThrow();
        assertEquals(EstadoReservaPreventa.EN_COMPRA, enCompra.getEstado());

        mockMvc.perform(post("/api/compras/{id}/cancelar", compraId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        ReservaPreventa reactivada = reservaPreventaRepository.buscarPorId(UUID.fromString(reservaId)).orElseThrow();
        assertEquals(EstadoReservaPreventa.ACTIVA, reactivada.getEstado());
        assertEquals(EstadoUnidad.RESERVADA,
                componenteRepository.buscarPorId(componenteId).orElseThrow()
                        .getUnidades().getFirst().getEstado());

        MvcResult segundaCompra = mockMvc.perform(post("/api/reservas-preventa/{id}/compra", reservaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compradorId":"%s"}
                                """.formatted(compradorId)))
                .andExpect(status().isCreated())
                .andReturn();
        String segundaCompraId = JsonPath.read(segundaCompra.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/compras/{id}/confirmar", segundaCompraId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));

        ReservaPreventa convertida = reservaPreventaRepository.buscarPorId(UUID.fromString(reservaId)).orElseThrow();
        assertEquals(EstadoReservaPreventa.CONVERTIDA, convertida.getEstado());
        assertEquals(EstadoUnidad.VENDIDA,
                componenteRepository.buscarPorId(componenteId).orElseThrow()
                        .getUnidades().getFirst().getEstado());
    }

    @Test
    void cancelarLaReservaAntesDeLaLlegadaDevuelveLaUnidadAPreventa() throws Exception {
        String vendedorId = id("v");
        String componenteId = id("gpu");
        crearVendedor(vendedorId);
        crearComponente(componenteId, vendedorId);
        UUID unidadId = publicarPreventa(componenteId, vendedorId);
        String compradorId = id("comprador");
        String reservaId = reservar(componenteId, unidadId, compradorId);

        mockMvc.perform(delete("/api/reservas-preventa/{id}", reservaId)
                        .param("compradorId", "otro-comprador"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/reservas-preventa/{id}", reservaId)
                        .param("compradorId", compradorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        mockMvc.perform(get("/api/componentes/{id}", componenteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockPreventa").value(1))
                .andExpect(jsonPath("$.unidades[0].estado").value("PREVENTA"))
                .andExpect(jsonPath("$.unidades[0].reservaPreventaId").doesNotExist());

        ReservaPreventa reserva = reservaPreventaRepository.buscarPorId(UUID.fromString(reservaId)).orElseThrow();
        assertEquals(EstadoReservaPreventa.CANCELADA, reserva.getEstado());
    }

    @Test
    void recibirUnaPreventaSinReservaLaDejaDisponibleYExigeAlDueno() throws Exception {
        String vendedorId = id("v");
        String otroVendedorId = id("v");
        String componenteId = id("gpu");
        crearVendedor(vendedorId);
        crearVendedor(otroVendedorId);
        crearComponente(componenteId, vendedorId);
        UUID unidadId = publicarPreventa(componenteId, vendedorId);

        mockMvc.perform(post("/api/componentes/{id}/unidades/{unidadId}/recibir", componenteId, unidadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"%s","numeroSerie":"SER-NO-DUENO"}
                                """.formatted(otroVendedorId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("dueño")));

        recibir(componenteId, unidadId, vendedorId, "SER-DISPONIBLE");

        Componente componente = componenteRepository.buscarPorId(componenteId).orElseThrow();
        assertEquals(EstadoUnidad.DISPONIBLE, componente.getUnidades().getFirst().getEstado());
        assertNotNull(componente.getUnidades().getFirst().getNumeroSerie());
        assertEquals(1, componente.stockDisponible());
    }
}
