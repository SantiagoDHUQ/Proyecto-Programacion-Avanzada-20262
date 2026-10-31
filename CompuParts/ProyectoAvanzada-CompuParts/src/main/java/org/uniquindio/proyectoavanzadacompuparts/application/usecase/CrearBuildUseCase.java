package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.factory.BuildFactory;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;

import java.util.List;

/**
 * Caso de uso que crea un nuevo Build con los componentes indicados.
 */
@Service
@RequiredArgsConstructor
public class CrearBuildUseCase {

    private final BuildRepository buildRepository;
    private final ComponenteRepository componenteRepository;
    private final BuildFactory buildFactory;

    @Transactional
    public Build ejecutar(List<String> componenteIds) {
        List<Componente> componentes = componenteRepository.buscarPorIds(componenteIds);
        Build build = buildFactory.crearCon(componentes);
        return buildRepository.guardar(build);
    }

    @Transactional
    public Build ejecutar() {
        Build build = buildFactory.nuevo();
        return buildRepository.guardar(build);
    }
}
