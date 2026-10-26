package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.response.BuildResponse;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.AgregarComponenteABuildUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarBuildUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.CrearBuildUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.MarcarBuildListoParaCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ValidarCompatibilidadBuildUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest.mapper.BuildMapper;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BuildController.class)
class BuildControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrearBuildUseCase crearBuildUseCase;

    @MockitoBean
    private ConsultarBuildUseCase consultarBuildUseCase;

    @MockitoBean
    private AgregarComponenteABuildUseCase agregarComponenteABuildUseCase;

    @MockitoBean
    private ValidarCompatibilidadBuildUseCase validarCompatibilidadBuildUseCase;

    @MockitoBean
    private MarcarBuildListoParaCompraUseCase marcarBuildListoParaCompraUseCase;

    @MockitoBean
    private BuildMapper mapper;

    @Test
    void debeCrearUnBuild() throws Exception {
        Build build = Build.crear();
        BuildResponse response = BuildResponse.from(build);
        when(crearBuildUseCase.ejecutar()).thenReturn(build);
        when(mapper.toResponse(build)).thenReturn(response);

        mockMvc.perform(post("/api/builds"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/builds/" + build.getId())))
                .andExpect(jsonPath("$.buildId").value(build.getId().toString()))
                .andExpect(jsonPath("$.estado").value("EN_CONSTRUCCION"));

        verify(mapper).toResponse(build);
    }

    @Test
    void debeConsultarUnBuildExistente() throws Exception {
        UUID buildId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        BuildResponse response = new BuildResponse(
                buildId.toString(), "EN_CONSTRUCCION", List.of(), false, List.of());
        when(consultarBuildUseCase.ejecutar(buildId)).thenReturn(response);

        mockMvc.perform(get("/api/builds/{buildId}", buildId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buildId").value(buildId.toString()))
                .andExpect(jsonPath("$.estado").value("EN_CONSTRUCCION"));
    }

    @Test
    void debeAgregarUnComponenteAlBuild() throws Exception {
        UUID buildId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Build build = Build.crear();
        BuildResponse response = BuildResponse.from(build);
        when(agregarComponenteABuildUseCase.ejecutar(buildId, "comp-1")).thenReturn(build);
        when(mapper.toResponse(build)).thenReturn(response);

        mockMvc.perform(post("/api/builds/{buildId}/componentes", buildId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"componenteId":"comp-1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buildId").value(build.getId().toString()));

        verify(agregarComponenteABuildUseCase).ejecutar(buildId, "comp-1");
    }

    @Test
    void debeRetornar400CuandoFaltaElComponente() throws Exception {
        UUID buildId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        mockMvc.perform(post("/api/builds/{buildId}/componentes", buildId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("componenteId")));

        verifyNoInteractions(agregarComponenteABuildUseCase);
    }

    @Test
    void debeValidarLaCompatibilidadDelBuild() throws Exception {
        UUID buildId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        BuildResponse response = new BuildResponse(
                buildId.toString(), "EN_CONSTRUCCION", List.of(), false, List.of("Falta la PSU"));
        when(validarCompatibilidadBuildUseCase.ejecutar(buildId)).thenReturn(response);

        mockMvc.perform(post("/api/builds/{buildId}/validacion", buildId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compatible").value(false))
                .andExpect(jsonPath("$.motivos[0]").value("Falta la PSU"));
    }

    @Test
    void debeMarcarElBuildListoParaCompra() throws Exception {
        UUID buildId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Build build = Build.crear();
        BuildResponse response = BuildResponse.from(build);
        when(marcarBuildListoParaCompraUseCase.ejecutar(buildId)).thenReturn(build);
        when(mapper.toResponse(build)).thenReturn(response);

        mockMvc.perform(post("/api/builds/{buildId}/listo-para-compra", buildId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buildId").value(build.getId().toString()));

        verify(marcarBuildListoParaCompraUseCase).ejecutar(buildId);
    }
}
