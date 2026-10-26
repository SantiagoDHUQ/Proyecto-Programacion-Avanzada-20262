package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.Objects;

/**
 * Caso de uso del vendedor dueño: vuelve a publicar un componente agotado.
 *
 * <p>Un componente eliminado no puede revivirse; para eso existe el borrado
 * lógico y no se ofrece una operación de restauración.</p>
 */
public class PublicarComponenteUseCase {

    private final ComponenteRepository componenteRepository;

    public PublicarComponenteUseCase(ComponenteRepository componenteRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository);
    }

    public Componente ejecutar(String componenteId, String vendedorId) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));
        componente.validarPropietario(vendedorId);
        componente.publicar();
        return componenteRepository.guardar(componente);
    }
}
