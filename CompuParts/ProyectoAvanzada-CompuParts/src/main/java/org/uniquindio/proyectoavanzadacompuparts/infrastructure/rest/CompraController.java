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
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.RealizarCompraRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.ReembolsarCompraRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.response.CompraResponse;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.CancelarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConfirmarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RealizarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ReembolsarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;

import java.net.URI;

/**
 * API de compras: crear (reserva stock), confirmar (vende), cancelar (libera)
 * y reembolsar una compra completada con RMA aceptada (descarta las unidades).
 */
@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final RealizarCompraUseCase realizarCompraUseCase;
    private final ConfirmarCompraUseCase confirmarCompraUseCase;
    private final CancelarCompraUseCase cancelarCompraUseCase;
    private final ConsultarCompraUseCase consultarCompraUseCase;
    private final ReembolsarCompraUseCase reembolsarCompraUseCase;

    public CompraController(RealizarCompraUseCase realizarCompraUseCase,
                            ConfirmarCompraUseCase confirmarCompraUseCase,
                            CancelarCompraUseCase cancelarCompraUseCase,
                            ConsultarCompraUseCase consultarCompraUseCase,
                            ReembolsarCompraUseCase reembolsarCompraUseCase) {
        this.realizarCompraUseCase = realizarCompraUseCase;
        this.confirmarCompraUseCase = confirmarCompraUseCase;
        this.cancelarCompraUseCase = cancelarCompraUseCase;
        this.consultarCompraUseCase = consultarCompraUseCase;
        this.reembolsarCompraUseCase = reembolsarCompraUseCase;
    }

    @PostMapping
    public ResponseEntity<CompraResponse> realizar(@Valid @RequestBody RealizarCompraRequest request) {
        Compra compra = realizarCompraUseCase.ejecutar(request.buildId(), request.compradorId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(compra.getId())
                .toUri();
        return ResponseEntity.created(location).body(CompraResponse.from(compra));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraResponse> consultar(@PathVariable String id) {
        return ResponseEntity.ok(CompraResponse.from(consultarCompraUseCase.ejecutar(id)));
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<CompraResponse> confirmar(@PathVariable String id) {
        return ResponseEntity.ok(CompraResponse.from(confirmarCompraUseCase.ejecutar(id)));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<CompraResponse> cancelar(@PathVariable String id) {
        return ResponseEntity.ok(CompraResponse.from(cancelarCompraUseCase.ejecutar(id)));
    }

    @PostMapping("/{id}/reembolsar")
    public ResponseEntity<CompraResponse> reembolsar(@PathVariable String id,
                                                     @Valid @RequestBody ReembolsarCompraRequest request) {
        return ResponseEntity.ok(CompraResponse.from(
                reembolsarCompraUseCase.ejecutar(id, request.compradorId(), request.motivo())));
    }
}
