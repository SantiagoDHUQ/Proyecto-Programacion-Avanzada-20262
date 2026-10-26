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
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.OperacionVendedorRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.RechazarRMARequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.SolicitarRMARequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.response.SolicitudRMAResponse;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.AceptarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ListarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RechazarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.SolicitarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;

import java.net.URI;
import java.util.List;

/**
 * API de RMA: el comprador solicita y el vendedor dueño resuelve (regla 9).
 */
@RestController
@RequestMapping("/api/rma")
public class RMAController {

    private final SolicitarRMAUseCase solicitarRMAUseCase;
    private final AceptarRMAUseCase aceptarRMAUseCase;
    private final RechazarRMAUseCase rechazarRMAUseCase;
    private final ConsultarRMAUseCase consultarRMAUseCase;
    private final ListarRMAUseCase listarRMAUseCase;

    public RMAController(SolicitarRMAUseCase solicitarRMAUseCase,
                         AceptarRMAUseCase aceptarRMAUseCase,
                         RechazarRMAUseCase rechazarRMAUseCase,
                         ConsultarRMAUseCase consultarRMAUseCase,
                         ListarRMAUseCase listarRMAUseCase) {
        this.solicitarRMAUseCase = solicitarRMAUseCase;
        this.aceptarRMAUseCase = aceptarRMAUseCase;
        this.rechazarRMAUseCase = rechazarRMAUseCase;
        this.consultarRMAUseCase = consultarRMAUseCase;
        this.listarRMAUseCase = listarRMAUseCase;
    }

    @PostMapping
    public ResponseEntity<SolicitudRMAResponse> solicitar(@Valid @RequestBody SolicitarRMARequest request) {
        SolicitudRMA solicitud = solicitarRMAUseCase.ejecutar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(solicitud.getId())
                .toUri();
        return ResponseEntity.created(location).body(SolicitudRMAResponse.from(solicitud));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudRMAResponse> consultar(@PathVariable String id) {
        return ResponseEntity.ok(SolicitudRMAResponse.from(consultarRMAUseCase.ejecutar(id)));
    }

    @GetMapping
    public ResponseEntity<List<SolicitudRMAResponse>> listar() {
        return ResponseEntity.ok(listarRMAUseCase.ejecutar().stream()
                .map(SolicitudRMAResponse::from)
                .toList());
    }

    @PostMapping("/{id}/aceptar")
    public ResponseEntity<SolicitudRMAResponse> aceptar(@PathVariable String id,
                                                        @Valid @RequestBody OperacionVendedorRequest request) {
        return ResponseEntity.ok(SolicitudRMAResponse.from(
                aceptarRMAUseCase.ejecutar(id, request.vendedorId())));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<SolicitudRMAResponse> rechazar(@PathVariable String id,
                                                         @Valid @RequestBody RechazarRMARequest request) {
        return ResponseEntity.ok(SolicitudRMAResponse.from(
                rechazarRMAUseCase.ejecutar(id, request.vendedorId(), request.motivo())));
    }
}
