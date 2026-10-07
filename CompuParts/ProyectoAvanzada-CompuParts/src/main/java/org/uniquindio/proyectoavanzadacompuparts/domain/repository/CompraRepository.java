package org.uniquindio.proyectoavanzadacompuparts.domain.repository;

import java.util.Optional;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;

public interface CompraRepository {
    Optional<Compra> obtenerPorId(String id);
    void guardar(Compra compra);
}