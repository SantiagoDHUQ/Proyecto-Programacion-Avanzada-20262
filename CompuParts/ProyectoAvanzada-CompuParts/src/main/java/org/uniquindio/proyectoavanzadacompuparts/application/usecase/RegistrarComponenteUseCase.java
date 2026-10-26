package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.RegistrarComponenteRequest;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.util.Objects;

/**
 * Caso de uso que registra un nuevo Componente (SKU) en el catálogo.
 *
 * <p>El vendedor debe existir y ser el dueño del SKU. Las unidades físicas se
 * agregan con {@link AgregarUnidadUseCase} para que cada una tenga su serial.</p>
 */
public class RegistrarComponenteUseCase {

    private final ComponenteRepository componenteRepository;
    private final VendedorRepository vendedorRepository;

    public RegistrarComponenteUseCase(ComponenteRepository componenteRepository,
                                      VendedorRepository vendedorRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository);
        this.vendedorRepository = Objects.requireNonNull(vendedorRepository);
    }

    public Componente ejecutar(RegistrarComponenteRequest request) {
        if (componenteRepository.buscarPorId(request.id()).isPresent()) {
            throw new ReglaDominioException("Ya existe un Componente con ese identificador");
        }

        Vendedor vendedor = vendedorRepository.buscarPorId(request.vendedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Vendedor con ese identificador"));

        Componente componente = Componente.crear(
                request.id(),
                request.nombre(),
                parseCategoria(request.categoria()),
                new EspecificacionTecnica(request.socket(), request.wattajeRequerido(), request.tipoMemoria()),
                new Precio(request.precio(), request.moneda()),
                vendedor,
                request.duracionGarantia()
        );

        return componenteRepository.guardar(componente);
    }

    private CategoriaComponente parseCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            throw new ReglaDominioException("La categoría de componente es obligatoria");
        }
        try {
            return CategoriaComponente.valueOf(categoria.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ReglaDominioException("Categoría de componente inválida: " + categoria);
        }
    }
}
