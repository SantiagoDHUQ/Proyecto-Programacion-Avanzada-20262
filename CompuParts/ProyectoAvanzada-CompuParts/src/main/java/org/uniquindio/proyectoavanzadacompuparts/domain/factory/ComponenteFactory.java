package org.uniquindio.proyectoavanzadacompuparts.domain.factory;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

public final class ComponenteFactory {

    public Componente registrar(Vendedor vendedor, DatosComponente datos, boolean idYaRegistrado) {
        exigir(datos != null, "Los datos del componente son obligatorios");
        exigir(!idYaRegistrado, "Ya existe un Componente con ese identificador");

        return Componente.crear(
                datos.id(),
                datos.nombre(),
                CategoriaComponente.desde(datos.categoria()),
                new EspecificacionTecnica(datos.socket(), datos.wattajeRequerido(), datos.tipoMemoria()),
                new Precio(datos.precio(), datos.moneda()),
                vendedor,
                datos.duracionGarantia());
    }
}