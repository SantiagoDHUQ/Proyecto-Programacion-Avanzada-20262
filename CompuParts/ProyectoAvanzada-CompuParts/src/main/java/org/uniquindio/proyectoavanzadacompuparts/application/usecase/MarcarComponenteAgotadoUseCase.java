package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.Objects;

/**
 * Caso de uso que marca un Componente como agotado.
 *
 * <p>Es la operación permitida incluso cuando el componente está dentro de un
 * Build activo: se oculta de la venta sin borrar su historial ni sus unidades.</p>
 */
public class MarcarComponenteAgotadoUseCase {

    private final ComponenteRepository componenteRepository;

    public MarcarComponenteAgotadoUseCase(ComponenteRepository componenteRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository);
    }

    public Componente ejecutar(String componenteId, String vendedorId) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));

        componente.validarPropietario(vendedorId);
        componente.marcarAgotado();
        return componenteRepository.guardar(componente);
    }
}
