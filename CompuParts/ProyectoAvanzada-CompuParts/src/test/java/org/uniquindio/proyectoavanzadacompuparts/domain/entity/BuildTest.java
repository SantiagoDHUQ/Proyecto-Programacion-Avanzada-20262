package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class BuildTest {
    @Test
    void buildIncompletoNoDebeMarcarseListoParaCompra() {
        // Arrange
        Build build = Build.crear();

        // Act & Assert
        assertThrows(
                ReglaDominioException.class,
                () -> build.marcarComoListoParaCompra()
        );
    }

    @Test
    void buildIncompletoDebeConservarSuEstadoTrasElRechazo() {
        // Arrange
        Build build = Build.crear();
        EstadoBuild estadoAnterior = build.getEstado();

        // Act
        assertThrows(
                ReglaDominioException.class,
                () -> build.marcarComoListoParaCompra()
        );

        // Assert
        assertEquals(estadoAnterior, build.getEstado());
    }

    @Test
    void buildIncompleto() {
        //Arrange
        EspecificacionTecnica espTec = new EspecificacionTecnica("AMR5", 0, "DDR5");
        Precio base = new Precio(new BigDecimal(1000), "USD");
        Vendedor ven = Vendedor.crear("Juan", "Autorizado");
        Componente comp = Componente.crear("R", CategoriaComponente.CPU, espTec, base, Disponibilidad.DISPONIBLE, ven, "123");
        Build build = Build.crear();
        build.agregarComponente(comp);

        //Act & Assert
        assertFalse(build.esCompleto());
    }
}
