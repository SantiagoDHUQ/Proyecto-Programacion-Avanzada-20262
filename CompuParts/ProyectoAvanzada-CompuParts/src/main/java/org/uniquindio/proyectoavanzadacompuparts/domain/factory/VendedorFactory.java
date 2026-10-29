package org.uniquindio.proyectoavanzadacompuparts.domain.factory;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

public final class VendedorFactory {

    public Vendedor registrar(String id, String nombre, String tipo, boolean idYaRegistrado) {
        exigir(id != null && !id.isBlank(), "El identificador no puede estar vacío");
        exigir(nombre != null && !nombre.isBlank(), "El nombre no puede estar vacío");
        exigir(tipo != null && !tipo.isBlank(), "El tipo no puede estar vacío");
        exigir(!idYaRegistrado, "Ya existe un Vendedor con ese identificador");

        return Vendedor.crear(id, nombre, tipo);
    }
}