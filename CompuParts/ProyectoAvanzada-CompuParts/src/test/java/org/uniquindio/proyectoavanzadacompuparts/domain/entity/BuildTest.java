package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BuildTest {

    //Arrange reutilizado ajajaja
    private final Vendedor autorizado = Vendedor.crear("v1", "Distribuidor Oficial", "AUTORIZADO");
    private final Vendedor particular = Vendedor.crear("v2", "Juan (usado)", "PARTICULAR");
    private final EspecificacionTecnica spec = new EspecificacionTecnica("AM5", 100, "DDR5");
    private final Precio precio = new Precio(new BigDecimal("1500000"), "COP");

    private Componente crearVacio() {
        return Componente.crear("", "", null, null, null, null, "", null);
    }

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
    void lanzaReglaDominioExceptionAlAgregarUnComponeneteNull() {

        //Arrange
        Build build = Build.crear();

        //Act & Assert
        assertThrows(ReglaDominioException.class, () -> build.agregarComponente(null));
    }
}
