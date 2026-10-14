package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class BuildTest {

    //Arrange
    private Componente componente(String id, CategoriaComponente categoria, String socket, int wattaje) {
        Vendedor vendedor = Vendedor.crear("v1", "Distribuidor Oficial", "AUTORIZADO");
        return Componente.crear(id, "pieza-" + id, categoria,
                new EspecificacionTecnica(socket, wattaje, "DDR5"),
                new Precio(new BigDecimal("100000"), "COP"), vendedor, "SER-" + id, 365);
    }

    private Build buildCompleto(int wattajePsu) {
        Build build = Build.crear();
        build.agregarComponente(componente("cpu", CategoriaComponente.CPU, "AM5", 120));
        build.agregarComponente(componente("mb", CategoriaComponente.MOTHERBOARD, "AM5", 80));
        build.agregarComponente(componente("psu", CategoriaComponente.PSU, "AM5", wattajePsu));
        return build;
    }

    @Test
    void buildIncompletoNoDebeMarcarseListoParaCompra() {
        // Arrange
        Build build = Build.crear();

        // Act & Assert
        assertThrows(
                ReglaDominioException.class, () -> build.marcarComoListoParaCompra()
        );
    }

    @Test
    void buildIncompletoDebeConservarSuEstadoTrasElRechazo() {
        // Arrange
        Build build = Build.crear();
        EstadoBuild estadoAnterior = build.getEstado();

        // Act
        assertThrows(
                ReglaDominioException.class, () -> build.marcarComoListoParaCompra()
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

    @Test
    void psuInsuficienteDebeLanzarReglaDominioExceptionAlValidar() {
        //Arrange (consumo de 200w)
        Build build = buildCompleto(100);

        //Act & Assert
        assertThrows(ReglaDominioException.class, () -> build.validarCompatibilidad());
    }

    @Test
    void buildCompletoYCompatibleDebeQuedarListoParaCompra() {
        //Arrange
        Build build = buildCompleto(500);
        build.validarCompatibilidad();
        build.marcarComoListoParaCompra();

        //Act & Assert
        assertEquals(EstadoBuild.LISTO_PARA_COMPRA, build.getEstado());
    }

    @Test
    void socketDistintoMarcaElBuildComoIncompatible() {
        //Arrange
        Build build = buildCompleto(500);
        build.eliminarComponente("mb");
        build.agregarComponente(componente("mb", CategoriaComponente.MOTHERBOARD, "LGA1700", 100));

        //Act & Assert
        assertThrows(ReglaDominioException.class, () -> build.validarCompatibilidad());
        assertEquals(EstadoBuild.INCOMPATIBLE, build.getEstado());
    }

    @Test
    void psuInsuficienteDebeMarcarElBuildComoIncompatible() {
        //Arrange
        Build build = buildCompleto(100);
        //Act
        assertThrows(ReglaDominioException.class, () -> build.validarCompatibilidad());
        //Assert
        assertEquals(EstadoBuild.INCOMPATIBLE, build.getEstado());
    }

    @Test
    void validarUnBuildListoYCompatibleNoDebeDegradarSuEstado() {
        //Arrange
        Build build = buildCompleto(500);
        build.marcarComoListoParaCompra();
        //Act
        build.validarCompatibilidad();
        //Assert
        assertEquals(EstadoBuild.LISTO_PARA_COMPRA, build.getEstado());
    }
}
