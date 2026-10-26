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
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.ComprarReservaPreventaRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.ReservarPreventaRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.response.CompraResponse;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.response.ReservaPreventaResponse;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.CancelarReservaPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ComprarReservaPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarReservaPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ReservarPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;

import java.net.URI;
import java.util.UUID;

/**
 * API de reservas de preventa.
 *
 * <p>El comprador reserva una unidad publicada en preventa, puede cancelar la
 * reserva y, cuando la unidad llega, convertirla en una compra directa.</p>
 */
@RestController
@RequestMapping("/api")
public class ReservaPreventaController {

    private final ReservarPreventaUseCase reservarPreventaUseCase;
    private final CancelarReservaPreventaUseCase cancelarReservaPreventaUseCase;
    private final ConsultarReservaPreventaUseCase consultarReservaPreventaUseCase;
    private final ComprarReservaPreventaUseCase comprarReservaPreventaUseCase;

    public ReservaPreventaController(ReservarPreventaUseCase reservarPreventaUseCase,
                                     CancelarReservaPreventaUseCase cancelarReservaPreventaUseCase,
                                     ConsultarReservaPreventaUseCase consultarReservaPreventaUseCase,
                                     ComprarReservaPreventaUseCase comprarReservaPreventaUseCase) {
        this.reservarPreventaUseCase = reservarPreventaUseCase;
        this.cancelarReservaPreventaUseCase = cancelarReservaPreventaUseCase;
        this.consultarReservaPreventaUseCase = consultarReservaPreventaUseCase;
        this.comprarReservaPreventaUseCase = comprarReservaPreventaUseCase;
    }

    @PostMapping("/componentes/{componenteId}/unidades/{unidadId}/reservas-preventa")
    public ResponseEntity<ReservaPreventaResponse> reservar(@PathVariable String componenteId,
                                                            @PathVariable UUID unidadId,
                                                            @Valid @RequestBody ReservarPreventaRequest request) {
        ReservaPreventa reserva = reservarPreventaUseCase.ejecutar(
                componenteId, unidadId, request.compradorId());
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/reservas-preventa/{id}")
                .buildAndExpand(reserva.getId())
                .toUri();
        return ResponseEntity.created(location).body(ReservaPreventaResponse.from(reserva));
    }

    @GetMapping("/reservas-preventa/{id}")
    public ResponseEntity<ReservaPreventaResponse> consultar(@PathVariable UUID id) {
        return ResponseEntity.ok(ReservaPreventaResponse.from(consultarReservaPreventaUseCase.ejecutar(id)));
    }

    @DeleteMapping("/reservas-preventa/{id}")
    public ResponseEntity<ReservaPreventaResponse> cancelar(@PathVariable UUID id,
                                                            @RequestParam String compradorId) {
        return ResponseEntity.ok(ReservaPreventaResponse.from(
                cancelarReservaPreventaUseCase.ejecutar(id, compradorId)));
    }

    @PostMapping("/reservas-preventa/{id}/compra")
    public ResponseEntity<CompraResponse> comprar(@PathVariable UUID id,
                                                  @Valid @RequestBody ComprarReservaPreventaRequest request) {
        Compra compra = comprarReservaPreventaUseCase.ejecutar(id, request.compradorId());
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/compras/{id}")
                .buildAndExpand(compra.getId())
                .toUri();
        return ResponseEntity.created(location).body(CompraResponse.from(compra));
    }
}
