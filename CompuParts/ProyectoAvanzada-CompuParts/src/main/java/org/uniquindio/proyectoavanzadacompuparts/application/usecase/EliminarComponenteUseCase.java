package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.Objects;

/**
 * Caso de uso que elimina lógicamente un Componente del catálogo.
 *
 * <p>No se permite eliminarlo si participa en un Build activo: en ese caso el
 * vendedor solo puede marcarlo como agotado (regla 6). El borrado es lógico para
 * conservar el historial de compras, RMA y unidades.</p>
 */
@Service
@RequiredArgsConstructor
public class EliminarComponenteUseCase {

    private final ComponenteRepository componenteRepository;
    private final BuildRepository buildRepository;

    @Transactional
    public void ejecutar(String componenteId, String vendedorId) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));

        componente.validarPropietario(vendedorId);

        boolean estaEnBuildActivo = buildRepository.listarTodos().stream()
                .filter(Build::estaActivo)
                .anyMatch(build -> build.contieneComponente(componenteId));

        if (estaEnBuildActivo) {
            throw new ReglaDominioException("El componente pertenece a un Build activo; solo puede marcarse como agotado");
        }

        componente.eliminar();
        componenteRepository.guardar(componente);
    }
}
