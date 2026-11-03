package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class PrecioEmbeddable {

    private BigDecimal monto;
    private String moneda;

    public PrecioEmbeddable(BigDecimal monto, String moneda) {
        this.monto = monto;
        this.moneda = moneda;
    }
}
