package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.AgregarUnidadUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarComponenteUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarUnidadUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.EliminarComponenteUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ListarComponentesUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.MarcarComponenteAgotadoUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.PublicarComponenteUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.PublicarPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RecibirPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RegistrarComponenteUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComponenteController.class)
class ComponenteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarComponenteUseCase registrarComponenteUseCase;

    @MockitoBean
    private ConsultarComponenteUseCase consultarComponenteUseCase;

    @MockitoBean
    private ListarComponentesUseCase listarComponentesUseCase;

    @MockitoBean
    private AgregarUnidadUseCase agregarUnidadUseCase;

    @MockitoBean
    private ConsultarUnidadUseCase consultarUnidadUseCase;

    @MockitoBean
    private PublicarPreventaUseCase publicarPreventaUseCase;

    @MockitoBean
    private RecibirPreventaUseCase recibirPreventaUseCase;

    @MockitoBean
    private MarcarComponenteAgotadoUseCase marcarComponenteAgotadoUseCase;

    @MockitoBean
    private PublicarComponenteUseCase publicarComponenteUseCase;

    @MockitoBean
    private EliminarComponenteUseCase eliminarComponenteUseCase;

    @Test
    void debeRegistrarUnComponenteCuandoLosDatosSonValidos() throws Exception {
        when(registrarComponenteUseCase.ejecutar(any())).thenReturn(componenteConUnidad());

        mockMvc.perform(post("/api/componentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id":"comp-1","nombre":"CPU","categoria":"CPU",
                                  "socket":"AM5","wattajeRequerido":120,"tipoMemoria":"DDR5",
                                  "moneda":"COP","precio":100000,"duracionGarantia":365,
                                  "vendedorId":"v-1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/componentes/comp-1")))
                .andExpect(jsonPath("$.id").value("comp-1"))
                .andExpect(jsonPath("$.vendedorId").value("v-1"))
                .andExpect(jsonPath("$.estadoComercial").value("PUBLICADO"))
                .andExpect(jsonPath("$.stockDisponible").value(1))
                .andExpect(jsonPath("$.unidades[0].numeroSerie").value("SER-1"));

        verify(registrarComponenteUseCase).ejecutar(any());
    }

    @Test
    void debeRetornar400CuandoFaltaElVendedor() throws Exception {
        mockMvc.perform(post("/api/componentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id":"comp-1","nombre":"CPU","categoria":"CPU",
                                  "wattajeRequerido":120,"moneda":"COP","precio":100000,
                                  "duracionGarantia":365
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("vendedorId")));

        verifyNoInteractions(registrarComponenteUseCase);
    }

    @Test
    void debeConsultarUnComponenteConSusUnidades() throws Exception {
        when(consultarComponenteUseCase.ejecutar("comp-1")).thenReturn(componenteConUnidad());

        mockMvc.perform(get("/api/componentes/{id}", "comp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("comp-1"))
                .andExpect(jsonPath("$.garantiaDias").value(365))
                .andExpect(jsonPath("$.stockDisponible").value(1))
                .andExpect(jsonPath("$.unidades[0].estado").value("DISPONIBLE"));
    }

    @Test
    void debeRetornar404CuandoElComponenteNoExiste() throws Exception {
        when(consultarComponenteUseCase.ejecutar("comp-inexistente"))
                .thenThrow(new RecursoNoEncontradoException("No existe el componente"));

        mockMvc.perform(get("/api/componentes/{id}", "comp-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el componente"));
    }

    @Test
    void debeListarElCatalogo() throws Exception {
        when(listarComponentesUseCase.ejecutar()).thenReturn(List.of(componenteConUnidad()));

        mockMvc.perform(get("/api/componentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("comp-1"));
    }

    @Test
    void debeAgregarUnaUnidadAlComponente() throws Exception {
        when(agregarUnidadUseCase.ejecutar("comp-1", "v-1", "SER-2"))
                .thenReturn(componenteConUnidad());

        mockMvc.perform(post("/api/componentes/{id}/unidades", "comp-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"v-1","numeroSerie":"SER-2"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockDisponible").value(1));

        verify(agregarUnidadUseCase).ejecutar("comp-1", "v-1", "SER-2");
    }

    @Test
    void debeRetornar400CuandoLaUnidadNoTieneNumeroDeSerie() throws Exception {
        mockMvc.perform(post("/api/componentes/{id}/unidades", "comp-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"v-1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("numeroSerie")));

        verifyNoInteractions(agregarUnidadUseCase);
    }

    @Test
    void debeConsultarUnaUnidadDelComponente() throws Exception {
        Componente componente = componenteConUnidad();
        UnidadInventario unidad = componente.getUnidades().getFirst();
        when(consultarUnidadUseCase.ejecutar("comp-1", unidad.getId())).thenReturn(unidad);

        mockMvc.perform(get("/api/componentes/{id}/unidades/{unidadId}", "comp-1", unidad.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(unidad.getId().toString()))
                .andExpect(jsonPath("$.numeroSerie").value("SER-1"))
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));
    }

    @Test
    void debeRetornar404CuandoLaUnidadNoExiste() throws Exception {
        UUID unidadId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(consultarUnidadUseCase.ejecutar("comp-1", unidadId))
                .thenThrow(new RecursoNoEncontradoException("No existe la unidad"));

        mockMvc.perform(get("/api/componentes/{id}/unidades/{unidadId}", "comp-1", unidadId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la unidad"));
    }

    @Test
    void debePublicarUnaPreventa() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(10);
        when(publicarPreventaUseCase.ejecutar("comp-1", "v-1", fecha))
                .thenReturn(componenteConPreventa());

        mockMvc.perform(post("/api/componentes/{id}/preventas", "comp-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"v-1","fechaEstimadaLlegada":"%s"}
                                """.formatted(fecha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockPreventa").value(1))
                .andExpect(jsonPath("$.unidades[0].estado").value("PREVENTA"));

        verify(publicarPreventaUseCase).ejecutar("comp-1", "v-1", fecha);
    }

    @Test
    void debeRetornar400CuandoLaPreventaNoTieneFecha() throws Exception {
        mockMvc.perform(post("/api/componentes/{id}/preventas", "comp-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"v-1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("fechaEstimadaLlegada")));

        verifyNoInteractions(publicarPreventaUseCase);
    }

    @Test
    void debeRecibirUnaPreventa() throws Exception {
        Componente componente = componenteConUnidad();
        UnidadInventario unidad = componente.getUnidades().getFirst();
        when(recibirPreventaUseCase.ejecutar("comp-1", unidad.getId(), "v-1", "SER-1"))
                .thenReturn(componente);

        mockMvc.perform(post("/api/componentes/{id}/unidades/{unidadId}/recibir", "comp-1", unidad.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"v-1","numeroSerie":"SER-1"}
                                """))
                .andExpect(status().isOk());

        verify(recibirPreventaUseCase).ejecutar("comp-1", unidad.getId(), "v-1", "SER-1");
    }

    @Test
    void debeMarcarElComponenteComoAgotado() throws Exception {
        Componente componente = componenteConUnidad();
        componente.marcarAgotado();
        when(marcarComponenteAgotadoUseCase.ejecutar("comp-1", "v-1")).thenReturn(componente);

        mockMvc.perform(post("/api/componentes/{id}/agotado", "comp-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"v-1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoComercial").value("AGOTADO"));

        verify(marcarComponenteAgotadoUseCase).ejecutar("comp-1", "v-1");
    }

    @Test
    void debeVolverAPublicarElComponente() throws Exception {
        Componente componente = componenteConUnidad();
        componente.marcarAgotado();
        componente.publicar();
        when(publicarComponenteUseCase.ejecutar("comp-1", "v-1")).thenReturn(componente);

        mockMvc.perform(post("/api/componentes/{id}/publicar", "comp-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendedorId":"v-1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoComercial").value("PUBLICADO"));

        verify(publicarComponenteUseCase).ejecutar("comp-1", "v-1");
    }

    @Test
    void debeRetornar400CuandoLaOperacionDeVendedorNoIndicaVendedor() throws Exception {
        mockMvc.perform(post("/api/componentes/{id}/agotado", "comp-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("vendedorId")));

        verifyNoInteractions(marcarComponenteAgotadoUseCase);
    }

    @Test
    void debeEliminarLogicamenteElComponente() throws Exception {
        mockMvc.perform(delete("/api/componentes/{id}", "comp-1")
                        .param("vendedorId", "v-1"))
                .andExpect(status().isNoContent());

        verify(eliminarComponenteUseCase).ejecutar("comp-1", "v-1");
    }

    private Componente componenteConUnidad() {
        Componente componente = componenteBase();
        componente.agregarUnidad("SER-1");
        return componente;
    }

    private Componente componenteConPreventa() {
        Componente componente = componenteBase();
        componente.agregarPreventa(LocalDate.now().plusDays(10));
        return componente;
    }

    private Componente componenteBase() {
        Vendedor vendedor = Vendedor.crear("v-1", "Distribuidor Oficial", "AUTORIZADO");
        return Componente.crear(
                "comp-1",
                "CPU",
                CategoriaComponente.CPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"),
                new Precio(new BigDecimal("100000"), "COP"),
                vendedor,
                365
        );
    }
}
