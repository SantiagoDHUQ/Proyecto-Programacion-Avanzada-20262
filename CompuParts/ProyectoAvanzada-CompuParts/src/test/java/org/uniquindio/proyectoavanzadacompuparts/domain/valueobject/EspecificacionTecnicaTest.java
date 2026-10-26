package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import static org.junit.jupiter.api.Assertions.*;

public class EspecificacionTecnicaTest {
    @Test
    void dosEspecificacionesConMismosValoresDebenSerIguales(){
        //Arrange
        EspecificacionTecnica espTec1 = new EspecificacionTecnica("AW3",
                120, "DDR5");
        EspecificacionTecnica espTec2 = new EspecificacionTecnica("AW3",
                120, "DDR5");

        //Act & Assert
        assertEquals(espTec1, espTec2);
    }

    @Test
    void especificacionConWattajeNegativoDebeLanzarReglaDominioException() {
        //Arrange
        String socket = "AM5";
        int wattaje = -100;
        String tipoMemoria = "DDR5";

        //Act & Assert
        assertThrows(
                ReglaDominioException.class,
                () -> new EspecificacionTecnica(socket, wattaje, tipoMemoria)
        );
    }

    @Test
    void especificacionSinSocketEsValidaParaCategoriasQueNoLoTienen() {
        EspecificacionTecnica especificacion = new EspecificacionTecnica(null, 100, "DDR5");

        assertNull(especificacion.socket());
        assertEquals("DDR5", especificacion.tipoMemoria());
    }

    @Test
    void especificacionSinTipoMemoriaEsValidaParaCategoriasQueNoLoTienen() {
        EspecificacionTecnica especificacion = new EspecificacionTecnica("AM5", 120, null);

        assertEquals("AM5", especificacion.socket());
        assertNull(especificacion.tipoMemoria());
    }

    @Test
    void socketYMemoriaSeNormalizan() {
        EspecificacionTecnica especificacion = new EspecificacionTecnica(" am5 ", 120, " ddr5 ");

        assertEquals("AM5", especificacion.socket());
        assertEquals("DDR5", especificacion.tipoMemoria());
    }
}
