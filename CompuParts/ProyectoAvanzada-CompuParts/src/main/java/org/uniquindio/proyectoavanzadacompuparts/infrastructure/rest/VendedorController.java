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
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.RegistrarVendedorRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.response.VendedorResponse;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarVendedorUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ListarVendedoresUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RegistrarVendedorUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;

import java.net.URI;
import java.util.List;

/**
 * API de vendedores. El registro es la base para validar la regla 9 en las
 * operaciones de catálogo sin depender de autenticación.
 */
@RestController
@RequestMapping("/api/vendedores")
public class VendedorController {

    private final RegistrarVendedorUseCase registrarVendedorUseCase;
    private final ConsultarVendedorUseCase consultarVendedorUseCase;
    private final ListarVendedoresUseCase listarVendedoresUseCase;

    public VendedorController(RegistrarVendedorUseCase registrarVendedorUseCase,
                              ConsultarVendedorUseCase consultarVendedorUseCase,
                              ListarVendedoresUseCase listarVendedoresUseCase) {
        this.registrarVendedorUseCase = registrarVendedorUseCase;
        this.consultarVendedorUseCase = consultarVendedorUseCase;
        this.listarVendedoresUseCase = listarVendedoresUseCase;
    }

    @PostMapping
    public ResponseEntity<VendedorResponse> registrar(@Valid @RequestBody RegistrarVendedorRequest request) {
        Vendedor vendedor = registrarVendedorUseCase.ejecutar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(vendedor.getId())
                .toUri();
        return ResponseEntity.created(location).body(VendedorResponse.from(vendedor));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendedorResponse> consultar(@PathVariable String id) {
        return ResponseEntity.ok(VendedorResponse.from(consultarVendedorUseCase.ejecutar(id)));
    }

    @GetMapping
    public ResponseEntity<List<VendedorResponse>> listar() {
        return ResponseEntity.ok(listarVendedoresUseCase.ejecutar().stream()
                .map(VendedorResponse::from)
                .toList());
    }
}
