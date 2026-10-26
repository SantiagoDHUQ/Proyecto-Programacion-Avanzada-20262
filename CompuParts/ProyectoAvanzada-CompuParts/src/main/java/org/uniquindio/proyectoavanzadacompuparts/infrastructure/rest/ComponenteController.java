package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.AgregarUnidadRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.OperacionVendedorRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.PublicarPreventaRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.RecibirPreventaRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.RegistrarComponenteRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.response.ComponenteResponse;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.response.UnidadInventarioResponse;
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

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * API del catálogo de componentes y de sus unidades físicas.
 *
 * <p>El SKU se crea sin unidades; luego el vendedor dueño agrega unidades
 * disponibles, publica preventas y recibe las que llegaron.</p>
 */
@RestController
@RequestMapping("/api/componentes")
public class ComponenteController {

    private final RegistrarComponenteUseCase registrarComponenteUseCase;
    private final ConsultarComponenteUseCase consultarComponenteUseCase;
    private final ListarComponentesUseCase listarComponentesUseCase;
    private final AgregarUnidadUseCase agregarUnidadUseCase;
    private final ConsultarUnidadUseCase consultarUnidadUseCase;
    private final PublicarPreventaUseCase publicarPreventaUseCase;
    private final RecibirPreventaUseCase recibirPreventaUseCase;
    private final MarcarComponenteAgotadoUseCase marcarComponenteAgotadoUseCase;
    private final PublicarComponenteUseCase publicarComponenteUseCase;
    private final EliminarComponenteUseCase eliminarComponenteUseCase;

    public ComponenteController(RegistrarComponenteUseCase registrarComponenteUseCase,
                                ConsultarComponenteUseCase consultarComponenteUseCase,
                                ListarComponentesUseCase listarComponentesUseCase,
                                AgregarUnidadUseCase agregarUnidadUseCase,
                                ConsultarUnidadUseCase consultarUnidadUseCase,
                                PublicarPreventaUseCase publicarPreventaUseCase,
                                RecibirPreventaUseCase recibirPreventaUseCase,
                                MarcarComponenteAgotadoUseCase marcarComponenteAgotadoUseCase,
                                PublicarComponenteUseCase publicarComponenteUseCase,
                                EliminarComponenteUseCase eliminarComponenteUseCase) {
        this.registrarComponenteUseCase = registrarComponenteUseCase;
        this.consultarComponenteUseCase = consultarComponenteUseCase;
        this.listarComponentesUseCase = listarComponentesUseCase;
        this.agregarUnidadUseCase = agregarUnidadUseCase;
        this.consultarUnidadUseCase = consultarUnidadUseCase;
        this.publicarPreventaUseCase = publicarPreventaUseCase;
        this.recibirPreventaUseCase = recibirPreventaUseCase;
        this.marcarComponenteAgotadoUseCase = marcarComponenteAgotadoUseCase;
        this.publicarComponenteUseCase = publicarComponenteUseCase;
        this.eliminarComponenteUseCase = eliminarComponenteUseCase;
    }

    @PostMapping
    public ResponseEntity<ComponenteResponse> registrar(@Valid @RequestBody RegistrarComponenteRequest request) {
        Componente componente = registrarComponenteUseCase.ejecutar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(componente.getId())
                .toUri();
        return ResponseEntity.created(location).body(ComponenteResponse.from(componente));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComponenteResponse> consultar(@PathVariable String id) {
        return ResponseEntity.ok(ComponenteResponse.from(consultarComponenteUseCase.ejecutar(id)));
    }

    @GetMapping
    public ResponseEntity<List<ComponenteResponse>> listar() {
        return ResponseEntity.ok(listarComponentesUseCase.ejecutar().stream()
                .map(ComponenteResponse::from)
                .toList());
    }

    @PostMapping("/{id}/unidades")
    public ResponseEntity<ComponenteResponse> agregarUnidad(@PathVariable String id,
                                                            @Valid @RequestBody AgregarUnidadRequest request) {
        Componente componente = agregarUnidadUseCase.ejecutar(id, request.vendedorId(), request.numeroSerie());
        return ResponseEntity.ok(ComponenteResponse.from(componente));
    }

    @GetMapping("/{id}/unidades/{unidadId}")
    public ResponseEntity<UnidadInventarioResponse> consultarUnidad(@PathVariable String id,
                                                                    @PathVariable UUID unidadId) {
        return ResponseEntity.ok(UnidadInventarioResponse.from(
                consultarUnidadUseCase.ejecutar(id, unidadId)));
    }

    @PostMapping("/{id}/preventas")
    public ResponseEntity<ComponenteResponse> publicarPreventa(@PathVariable String id,
                                                               @Valid @RequestBody PublicarPreventaRequest request) {
        Componente componente = publicarPreventaUseCase.ejecutar(
                id, request.vendedorId(), request.fechaEstimadaLlegada());
        return ResponseEntity.ok(ComponenteResponse.from(componente));
    }

    @PostMapping("/{id}/unidades/{unidadId}/recibir")
    public ResponseEntity<ComponenteResponse> recibirPreventa(@PathVariable String id,
                                                              @PathVariable UUID unidadId,
                                                              @Valid @RequestBody RecibirPreventaRequest request) {
        Componente componente = recibirPreventaUseCase.ejecutar(
                id, unidadId, request.vendedorId(), request.numeroSerie());
        return ResponseEntity.ok(ComponenteResponse.from(componente));
    }

    @PostMapping("/{id}/agotado")
    public ResponseEntity<ComponenteResponse> marcarAgotado(@PathVariable String id,
                                                            @Valid @RequestBody OperacionVendedorRequest request) {
        Componente componente = marcarComponenteAgotadoUseCase.ejecutar(id, request.vendedorId());
        return ResponseEntity.ok(ComponenteResponse.from(componente));
    }

    @PostMapping("/{id}/publicar")
    public ResponseEntity<ComponenteResponse> publicar(@PathVariable String id,
                                                       @Valid @RequestBody OperacionVendedorRequest request) {
        Componente componente = publicarComponenteUseCase.ejecutar(id, request.vendedorId());
        return ResponseEntity.ok(ComponenteResponse.from(componente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id, @RequestParam String vendedorId) {
        eliminarComponenteUseCase.ejecutar(id, vendedorId);
        return ResponseEntity.noContent().build();
    }
}
