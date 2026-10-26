package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.AgregarComponenteABuildRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.response.BuildResponse;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.AgregarComponenteABuildUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarBuildUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.CrearBuildUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.MarcarBuildListoParaCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ValidarCompatibilidadBuildUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest.mapper.BuildMapper;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/builds")
public class BuildController {

    private final CrearBuildUseCase crearBuildUseCase;
    private final ConsultarBuildUseCase consultarBuildUseCase;
    private final AgregarComponenteABuildUseCase agregarComponenteABuildUseCase;
    private final ValidarCompatibilidadBuildUseCase validarCompatibilidadBuildUseCase;
    private final MarcarBuildListoParaCompraUseCase marcarBuildListoParaCompraUseCase;
    private final BuildMapper mapper;

    public BuildController(CrearBuildUseCase crearBuildUseCase,
                           ConsultarBuildUseCase consultarBuildUseCase,
                           AgregarComponenteABuildUseCase agregarComponenteABuildUseCase,
                           ValidarCompatibilidadBuildUseCase validarCompatibilidadBuildUseCase,
                           MarcarBuildListoParaCompraUseCase marcarBuildListoParaCompraUseCase,
                           BuildMapper mapper) {
        this.crearBuildUseCase = crearBuildUseCase;
        this.consultarBuildUseCase = consultarBuildUseCase;
        this.agregarComponenteABuildUseCase = agregarComponenteABuildUseCase;
        this.validarCompatibilidadBuildUseCase = validarCompatibilidadBuildUseCase;
        this.marcarBuildListoParaCompraUseCase = marcarBuildListoParaCompraUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<BuildResponse> crear() {
        Build build = crearBuildUseCase.ejecutar();

        BuildResponse response = mapper.toResponse(build);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(build.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{buildId}")
    public ResponseEntity<BuildResponse> consultar(@PathVariable UUID buildId) {
        return ResponseEntity.ok(consultarBuildUseCase.ejecutar(buildId));
    }

    @PostMapping("/{buildId}/componentes")
    public ResponseEntity<BuildResponse> agregarComponente(
            @PathVariable UUID buildId,
            @Valid @RequestBody AgregarComponenteABuildRequest request) {
        Build build = agregarComponenteABuildUseCase.ejecutar(buildId, request.componenteId());
        return ResponseEntity.ok(mapper.toResponse(build));
    }

    @PostMapping("/{buildId}/validacion")
    public ResponseEntity<BuildResponse> validarCompatibilidad(@PathVariable UUID buildId) {
        return ResponseEntity.ok(validarCompatibilidadBuildUseCase.ejecutar(buildId));
    }

    @PostMapping("/{buildId}/listo-para-compra")
    public ResponseEntity<BuildResponse> marcarListoParaCompra(@PathVariable UUID buildId) {
        Build build = marcarBuildListoParaCompraUseCase.ejecutar(buildId);
        return ResponseEntity.ok(mapper.toResponse(build));
    }
}
