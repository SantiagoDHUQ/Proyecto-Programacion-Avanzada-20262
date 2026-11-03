package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class EspecificacionTecnicaEmbeddable {

    private String socket;
    private int wattajeRequerido;
    private String tipoMemoria;

    public EspecificacionTecnicaEmbeddable(String socket, int wattajeRequerido, String tipoMemoria) {
        this.socket = socket;
        this.wattajeRequerido = wattajeRequerido;
        this.tipoMemoria = tipoMemoria;
    }
}
