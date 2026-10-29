package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CatalogoService;

import java.util.List;

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
    private final CatalogoService catalogoService;

    @Transactional
    public void ejecutar(String componenteId, String vendedorId) {
        Componente componente = componenteRepository.buscarPorId(componenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un Componente con ese identificador"));

        List<Build> buildsActivos = buildRepository.buscarActivosPorComponente(componenteId);

        catalogoService.eliminarComponente(componente, vendedorId, buildsActivos);

        componenteRepository.guardar(componente);
    }
}
