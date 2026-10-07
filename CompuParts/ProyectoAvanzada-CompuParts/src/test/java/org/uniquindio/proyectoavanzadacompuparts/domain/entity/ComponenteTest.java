package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Disponibilidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ComponenteTest {

    //Arrange general ajajaja
    private final Vendedor autorizado = Vendedor.crear("v1", "Distribuidor Oficial", "AUTORIZADO");
    private final Vendedor particular = Vendedor.crear("v2", "Juan (usado)", "PARTICULAR");
    private final EspecificacionTecnica spec = new EspecificacionTecnica("AM5", 100, "DDR5");
    private final Precio precio = new Precio(new BigDecimal("1500000"), "COP");

    private Componente crear(String id, Vendedor vendedor, Integer garantiaDias) {
        return Componente.crear(id, "RTX 4070", CategoriaComponente.GPU, spec, precio, vendedor, "GPU-" + id, garantiaDias);
    }
    @Test
    void componenteNoDebePasarAPreventaSinFechaEstimada() {

        //Arrange
        Componente c = crear("1", autorizado, 60);
        //Act & Assert
        assertThrows(ReglaDominioException.class, () -> c.pasarAPreventa(null));
    }

    @Test
    void vendedorAutorizadoConGarantiaCreaComponente() {

        //Arrange
        Componente c = crear("1", autorizado, 365);
        //Act & Assert
        assertEquals(Disponibilidad.DISPONIBLE, c.getDisponibilidad());
        assertEquals(365, c.getGarantia().orElseThrow().dias());
    }

    @Test
    void vendedorAutorizadoSinGarantiaSeRechaza() {

        //Arrange, Act & Assert
        assertThrows(ReglaDominioException.class, () -> crear("1", autorizado, null));
    }

    @Test
    void vendedorParticularConGarantiaSeRechaza() {

        //Arrange, Act & Assert
        assertThrows(ReglaDominioException.class, () -> crear("1", particular, 30));
    }

    @Test
    void vendedorParticularSinGarantiaCreaComponenteUsado() {

        //Arragne
        Componente c = crear("1", particular, null);

        //Act & Assert
        assertTrue(c.getGarantia().isEmpty());
    }

    @Test
    void datosObligatoriosFaltantesLanzanReglaDominio() {

        //Arrange, Act & Assert
        assertThrows(ReglaDominioException.class, () -> crear("", autorizado, 30));
        assertThrows(ReglaDominioException.class, () -> crear("1", null, 30));
        assertThrows(ReglaDominioException.class, () -> Componente.crear("1", " ", CategoriaComponente.GPU, spec, precio, autorizado, "S1", 30));
        assertThrows(ReglaDominioException.class, () -> Componente.crear("1", "x", CategoriaComponente.GPU, spec, precio, autorizado, " ", 30));
    }
}
