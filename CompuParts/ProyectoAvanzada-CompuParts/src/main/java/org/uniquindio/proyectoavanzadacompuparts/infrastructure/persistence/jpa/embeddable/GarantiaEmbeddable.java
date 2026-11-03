package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class GarantiaEmbeddable {

    private Integer dias;

    public GarantiaEmbeddable(Integer dias) {
        this.dias = dias;
    }
}
