package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import java.util.UUID;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Ítem de una compra: una unidad física concreta del inventario.
 *
 * <p>El número de serie y el precio se congelan al crear la compra. Así, aunque
 * el SKU cambie de precio o la unidad cambie de estado, queda el registro de lo
 * que realmente se compró.</p>
 */
public record ItemCompra(
        UUID unidadId,
        String componenteId,
        String numeroSerie,
        Precio precioUnitario
) {

    public ItemCompra {
        exigir(unidadId != null, "El ítem de compra debe indicar la unidad");
        exigir(componenteId != null && !componenteId.isBlank(),
                "El ítem de compra debe indicar el componente");
        exigir(numeroSerie != null && !numeroSerie.isBlank(),
                "El ítem de compra debe indicar el número de serie");
        exigir(precioUnitario != null, "El ítem de compra debe tener un precio válido");
    }
}
