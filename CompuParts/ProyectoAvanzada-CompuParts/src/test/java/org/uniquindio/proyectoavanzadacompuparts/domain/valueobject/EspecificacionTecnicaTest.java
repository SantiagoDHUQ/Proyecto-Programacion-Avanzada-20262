package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EspecificacionTecnicaTest {
    @Test
    void dosEspecificacionesConMismosValoresDebenSerIguales(){
        EspecificacionTecnica espTec1 = new EspecificacionTecnica("AW3",
                120, "DDR5");
        EspecificacionTecnica espTec2 = new EspecificacionTecnica("AW3",
                120, "DDR5");
        assertEquals(espTec1, espTec2);
    }

    @Test
    void especificacionConWattajeNegativoDebeLanzarReglaDominioException() {
        String socket = "AM5";
        int wattaje = -100;
        String tipoMemoria = "DDR5";

        assertThrows(
                ReglaDominioException.class,
                () -> new EspecificacionTecnica(socket, wattaje, tipoMemoria)
        );
    }
}
