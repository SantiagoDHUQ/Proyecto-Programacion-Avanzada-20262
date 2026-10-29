package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BuildTest {

    // Arrange
    private Componente componente(String id, CategoriaComponente categoria, String socket, int wattaje) {
        return componente(id, categoria, socket, wattaje, "DDR5");
    }

    private Componente componente(String id, CategoriaComponente categoria, String socket,
                                  int wattaje, String tipoMemoria) {
        Vendedor vendedor = Vendedor.crear("v1", "Distribuidor Oficial", "AUTORIZADO");
        Componente componente = Componente.crear(id, "pieza-" + id, categoria,
                new EspecificacionTecnica(socket, wattaje, tipoMemoria),
                new Precio(new BigDecimal("100000"), "COP"), vendedor, 365);
        componente.agregarUnidad("v1", "SER-" + id, false);
        return componente;
    }

    private Componente componenteSinStock(String id, CategoriaComponente categoria, String socket, int wattaje) {
        Vendedor vendedor = Vendedor.crear("v1", "Distribuidor Oficial", "AUTORIZADO");
        return Componente.crear(id, "pieza-" + id, categoria,
                new EspecificacionTecnica(socket, wattaje, "DDR5"),
                new Precio(new BigDecimal("100000"), "COP"), vendedor, 365);
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
        Build build = Build.crear();

        assertThrows(ReglaDominioException.class, build::marcarComoListoParaCompra);
    }

    @Test
    void buildIncompletoDebeConservarSuEstadoTrasElRechazo() {
        Build build = Build.crear();
        EstadoBuild estadoAnterior = build.getEstado();

        assertThrows(ReglaDominioException.class, build::marcarComoListoParaCompra);

        assertEquals(estadoAnterior, build.getEstado());
    }

    @Test
    void lanzaReglaDominioExceptionAlAgregarUnComponenteNull() {
        Build build = Build.crear();

        assertThrows(ReglaDominioException.class, () -> build.agregarComponente(null));
    }

    @Test
    void noSePuedeAgregarElMismoComponenteDosVeces() {
        Build build = Build.crear();
        Componente cpu = componente("cpu", CategoriaComponente.CPU, "AM5", 120);

        build.agregarComponente(cpu);

        assertThrows(ReglaDominioException.class, () -> build.agregarComponente(cpu));
    }

    @Test
    void noSePuedeAgregarUnComponenteSinStock() {
        Build build = Build.crear();
        Componente cpu = componenteSinStock("cpu", CategoriaComponente.CPU, "AM5", 120);

        assertThrows(ReglaDominioException.class, () -> build.agregarComponente(cpu));
    }

    @Test
    void noSePuedeAgregarUnComponenteEnPreventa() {
        Build build = Build.crear();
        Vendedor autorizado = Vendedor.crear("v1", "Distribuidor Oficial", "AUTORIZADO");
        Componente cpu = Componente.crear("cpu", "CPU en preventa", CategoriaComponente.CPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"),
                new Precio(new BigDecimal("100000"), "COP"), autorizado, 365);
        cpu.agregarPreventa("v1", LocalDate.now().plusDays(10), LocalDate.now());

        assertThrows(ReglaDominioException.class, () -> build.agregarComponente(cpu));
    }

    @Test
    void noSePuedeAgregarUnComponenteAgotado() {
        Build build = Build.crear();
        Componente cpu = componente("cpu", CategoriaComponente.CPU, "AM5", 120);
        cpu.marcarAgotado();

        assertThrows(ReglaDominioException.class, () -> build.agregarComponente(cpu));
    }

    @Test
    void noSePuedeAgregarUnComponenteEliminado() {
        Build build = Build.crear();
        Componente cpu = componente("cpu", CategoriaComponente.CPU, "AM5", 120);
        cpu.eliminar();

        assertThrows(ReglaDominioException.class, () -> build.agregarComponente(cpu));
    }

    @Test
    void psuInsuficienteDebeLanzarReglaDominioExceptionAlValidar() {
        Build build = buildCompleto(100);

        assertThrows(ReglaDominioException.class, build::validarCompatibilidad);
    }

    @Test
    void buildCompletoYCompatibleDebeQuedarListoParaCompra() {
        Build build = buildCompleto(500);
        build.validarCompatibilidad();
        build.marcarComoListoParaCompra();

        assertEquals(EstadoBuild.LISTO_PARA_COMPRA, build.getEstado());
    }

    @Test
    void buildCompletoPeroIncompatibleNoDebeQuedarListoParaCompra() {
        Build build = buildCompleto(100);

        assertThrows(ReglaDominioException.class, build::marcarComoListoParaCompra);
        assertEquals(EstadoBuild.INCOMPATIBLE, build.getEstado());
    }

    @Test
    void buildCompletoYCompatiblePeroSinStockNoDebeQuedarListoParaCompra() {
        Build build = buildCompleto(500);
        build.componentePorId("cpu").reservarPrimeraUnidadDisponible();

        assertThrows(ReglaDominioException.class, build::marcarComoListoParaCompra);
        assertEquals(EstadoBuild.EN_CONSTRUCCION, build.getEstado());
    }

    @Test
    void socketDistintoMarcaElBuildComoIncompatible() {
        Build build = buildCompleto(500);
        build.eliminarComponente("mb");
        build.agregarComponente(componente("mb", CategoriaComponente.MOTHERBOARD, "LGA1700", 100));

        assertThrows(ReglaDominioException.class, build::validarCompatibilidad);
        assertEquals(EstadoBuild.INCOMPATIBLE, build.getEstado());
    }

    @Test
    void memoriaRamDeTipoDistintoAlDeLaBoardMarcaElBuildComoIncompatible() {
        Build build = buildCompleto(500);
        build.agregarComponente(componente("ram", CategoriaComponente.RAM, null, 10, "DDR4"));

        assertThrows(ReglaDominioException.class, build::validarCompatibilidad);
        assertEquals(EstadoBuild.INCOMPATIBLE, build.getEstado());
    }

    @Test
    void memoriaRamCompatibleConLaBoardPermiteMarcarElBuildListo() {
        Build build = buildCompleto(500);
        build.agregarComponente(componente("ram", CategoriaComponente.RAM, null, 10, "DDR5"));

        build.marcarComoListoParaCompra();

        assertEquals(EstadoBuild.LISTO_PARA_COMPRA, build.getEstado());
    }

    @Test
    void unaBoardSinTipoMemoriaNoSeComparaConLaRam() {
        Build build = Build.crear();
        build.agregarComponente(componente("cpu", CategoriaComponente.CPU, "AM5", 120));
        build.agregarComponente(componente("mb", CategoriaComponente.MOTHERBOARD, "AM5", 80, null));
        build.agregarComponente(componente("psu", CategoriaComponente.PSU, null, 500, null));
        build.agregarComponente(componente("ram", CategoriaComponente.RAM, null, 10, "DDR5"));

        build.marcarComoListoParaCompra();

        assertEquals(EstadoBuild.LISTO_PARA_COMPRA, build.getEstado());
    }

    @Test
    void psuInsuficienteDebeMarcarElBuildComoIncompatible() {
        Build build = buildCompleto(100);

        assertThrows(ReglaDominioException.class, build::validarCompatibilidad);
        assertEquals(EstadoBuild.INCOMPATIBLE, build.getEstado());
    }

    @Test
    void validarUnBuildListoYCompatibleNoDebeDegradarSuEstado() {
        Build build = buildCompleto(500);
        build.marcarComoListoParaCompra();

        build.validarCompatibilidad();

        assertEquals(EstadoBuild.LISTO_PARA_COMPRA, build.getEstado());
    }

    @Test
    void agregarUnComponenteDevuelveElBuildAEnConstruccion() {
        Build build = buildCompleto(500);
        build.marcarComoListoParaCompra();

        build.agregarComponente(componente("gpu", CategoriaComponente.GPU, "AM5", 220));

        assertEquals(EstadoBuild.EN_CONSTRUCCION, build.getEstado());
    }

    @Test
    void noSePuedeEliminarUnComponenteDeUnBuildListoParaCompra() {
        Build build = buildCompleto(500);
        build.marcarComoListoParaCompra();

        assertThrows(ReglaDominioException.class, () -> build.eliminarComponente("cpu"));
    }

    @Test
    void unBuildSeMantieneActivoHastaComprarseOCancelarse() {
        Build build = buildCompleto(500);
        assertTrue(build.estaActivo());

        build.marcarComoListoParaCompra();
        assertTrue(build.estaActivo());
    }

    @Test
    void unBuildCompradoNoPuedeModificarseNiVolverAValidarseComoActivo() {
        Build build = buildCompleto(500);
        build.marcarComoListoParaCompra();
        build.marcarComoComprado();

        assertEquals(EstadoBuild.COMPRADO, build.getEstado());
        assertFalse(build.estaActivo());
        assertThrows(ReglaDominioException.class,
                () -> build.agregarComponente(componente("gpu", CategoriaComponente.GPU, "AM5", 220)));
        assertThrows(ReglaDominioException.class, () -> build.eliminarComponente("cpu"));
        assertThrows(ReglaDominioException.class, build::marcarComoListoParaCompra);

        build.validarCompatibilidad();
        assertEquals(EstadoBuild.COMPRADO, build.getEstado());
    }
}
