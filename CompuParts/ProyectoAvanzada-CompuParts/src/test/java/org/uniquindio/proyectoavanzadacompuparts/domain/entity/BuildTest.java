package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BuildTest {
    @Test
    void buildIncompletoNoDebeMarcarseListoParaCompra() {
        // Arrange
        Build build = Build.crear("1");

        // Act & Assert
        assertThrows(
                ReglaDominioException.class,
                () -> build.marcarComoListoParaCompra()
        );
    }

    @Test
    void buildIncompletoDebeConservarSuEstadoTrasElRechazo() {
        // Arrange
        Build build = Build.crear("1");
        EstadoBuild estadoAnterior = build.getEstado();

        // Act
        assertThrows(
                ReglaDominioException.class,
                () -> build.marcarComoListoParaCompra()
        );

        // Assert
        assertEquals(estadoAnterior, build.getEstado());
    }
}
