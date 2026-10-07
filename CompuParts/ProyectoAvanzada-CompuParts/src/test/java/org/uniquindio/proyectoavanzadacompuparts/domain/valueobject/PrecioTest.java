package org.uniquindio.proyectoavanzadacompuparts.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PrecioTest {
    @Test
    void precioConMontoNegativoDebeLanzarReglaDominioException() {

        //Arrange
        BigDecimal montoNegativo = new BigDecimal("-1");

        //Act & Assert
        assertThrows(ReglaDominioException.class, () -> new Precio(montoNegativo, "COP"));
    }
    @Test
    void precioLanzaReglaDominioExceptionAnteMontoNull() {

        //Arrange
        BigDecimal montoNulo = null;

        //Act & Assert
        assertThrows(ReglaDominioException.class, () -> new Precio(montoNulo, "USD"));
    }
    @Test
    void precioLanzaReglaDominioExceptionAnteMonedaVacia() {

        //Arrange
        BigDecimal montoMelo = new BigDecimal(150000);

        //Act & Assert
        assertThrows(ReglaDominioException.class, () -> new Precio(montoMelo, ""));
    }
    @Test
    void impideSumasMonedasDistintas() {

        //Arrange
        BigDecimal monto1 = new BigDecimal(10000);
        BigDecimal monto2 = new BigDecimal(5000);
        Precio precioUSD = new Precio(monto1, "USD");
        Precio precioCOP = new Precio(monto2, "COP");

        //Act & Assert
        assertThrows(ReglaDominioException.class, () -> precioCOP.sumar(precioUSD));
    }
    @Test
    void monedaEnMinusculaSirve() {

        //Arrange
        BigDecimal monto1 = new BigDecimal(5000);
        BigDecimal monto2 = new BigDecimal(5000);
        Precio precio1 = new Precio(monto1, "cop");
        Precio precio2 = new Precio(monto2, "COP");

        //Act & Assert
        assertEquals(precio1, precio2);
    }
}
