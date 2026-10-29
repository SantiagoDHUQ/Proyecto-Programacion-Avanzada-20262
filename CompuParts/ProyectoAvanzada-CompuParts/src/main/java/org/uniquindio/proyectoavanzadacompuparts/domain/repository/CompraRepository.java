package org.uniquindio.proyectoavanzadacompuparts.domain.repository;

import java.util.Optional;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;

public interface CompraRepository {
    Optional<Compra> buscarPorId(String id);
    Compra guardar(Compra compra);

    default java.util.Optional<org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra> obtenerItemPorNumeroSerie(Compra compra, String numeroSerie) {
        if (compra == null || numeroSerie == null) {
            return java.util.Optional.empty();
        }
        String serie = numeroSerie.trim();
        return compra.getItems().stream()
                .filter(i -> i != null && i.numeroSerie() != null && i.numeroSerie().equalsIgnoreCase(serie))
                .findFirst();
    }

    default String obtenerComponenteIdPorNumeroSerie(Compra compra, String numeroSerie) {
        return obtenerItemPorNumeroSerie(compra, numeroSerie)
                .map(org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra::componenteId)
                .orElse(null);
    }
}