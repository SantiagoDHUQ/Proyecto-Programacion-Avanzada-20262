package org.uniquindio.proyectoavanzadacompuparts.domain.factory;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;

import java.util.List;

public final class BuildFactory {

    public Build nuevo() {
        return Build.crear();
    }

    public Build crearCon(List<Componente> componentes) {
        return Build.crear(componentes);
    }
}