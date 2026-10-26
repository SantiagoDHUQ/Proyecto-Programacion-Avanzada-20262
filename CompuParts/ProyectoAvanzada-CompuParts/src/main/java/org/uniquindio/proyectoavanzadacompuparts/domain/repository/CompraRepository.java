package org.uniquindio.proyectoavanzadacompuparts.domain.repository;

import java.util.Optional;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;

public interface CompraRepository {
    Optional<Compra> buscarPorId(String id);
    Compra guardar(Compra compra);
}