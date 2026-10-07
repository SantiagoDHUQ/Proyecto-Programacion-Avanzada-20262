package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VendedorTest {
    @Test
    void vendedorTipoDesconocidoLanzaReglaDominioException() {

        //Arrange, Act & Assert
        assertThrows(ReglaDominioException.class, () -> Vendedor.crear("123", "Ramiro", "Mayorista"));
    }
    @Test
    void vendedorTipoAutorizadoEnMinusculaEsVerdadero(){

        //Arrange
        Vendedor vendedor = Vendedor.crear("1", "Juaco", "autorizado");

        //Act && Assert
        assertTrue(vendedor.esAutorizado());
    }
}
