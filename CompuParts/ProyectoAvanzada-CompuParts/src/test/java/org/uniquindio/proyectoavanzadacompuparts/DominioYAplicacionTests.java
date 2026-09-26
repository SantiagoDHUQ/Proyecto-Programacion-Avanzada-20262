package org.uniquindio.proyectoavanzadacompuparts;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Disponibilidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Garantia;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DominioYAplicacionTests {

    @Test
    void precioRechazaMontoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new Precio(new BigDecimal("-1.00"), "COP"));
    }

    @Test
    void garantiaEstaVigenteCuandoFechaActualEsAnteriorAlLimite() {
        Garantia garantia = new Garantia(LocalDate.now().minusDays(10), 365);
        assertTrue(garantia.estaVigente(LocalDate.now()));
    }

    @Test
    void componenteEnPreventaSinFechaEstimadaLanzaExcepcion() {
        Componente componente = Componente.crear(
                "Ryzen 7 7800X3D",
                CategoriaComponente.CPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"),
                new Precio(new BigDecimal("800000"), "COP"),
                Disponibilidad.DISPONIBLE,
                Vendedor.crear("Vendedor Test", "AUTORIZADO"),
                "SN-001"
        );

        assertThrows(ReglaDominioException.class, () -> componente.pasarAPreventa(null));
    }

    @Test
    void buildEsCompletoCuandoTieneMotherboardCpuYPsu() {
        Build build = Build.crear();
        build.agregarComponente(Componente.crear("Ryzen 7 7800X3D", CategoriaComponente.CPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"), new Precio(new BigDecimal("800000"), "COP"),
                Disponibilidad.DISPONIBLE, Vendedor.crear("Proveedor", "AUTORIZADO"), "SN-CPU"));
        build.agregarComponente(Componente.crear("MSI B650", CategoriaComponente.MOTHERBOARD,
                new EspecificacionTecnica("AM5", 80, "DDR5"), new Precio(new BigDecimal("700000"), "COP"),
                Disponibilidad.DISPONIBLE, Vendedor.crear("Proveedor", "AUTORIZADO"), "SN-MB"));
        build.agregarComponente(Componente.crear("Corsair RM850", CategoriaComponente.PSU,
                new EspecificacionTecnica("ATX", 850, "DDR5"), new Precio(new BigDecimal("900000"), "COP"),
                Disponibilidad.DISPONIBLE, Vendedor.crear("Proveedor", "AUTORIZADO"), "SN-PSU"));

        assertTrue(build.esCompleto());
    }

    @Test
    void buildConPsuInsuficienteLanzaExcepcion() {
        Build build = Build.crear();
        build.agregarComponente(Componente.crear("Ryzen 7 7800X3D", CategoriaComponente.CPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"), new Precio(new BigDecimal("800000"), "COP"),
                Disponibilidad.DISPONIBLE, Vendedor.crear("Proveedor", "AUTORIZADO"), "SN-CPU"));
        build.agregarComponente(Componente.crear("MSI B650", CategoriaComponente.MOTHERBOARD,
                new EspecificacionTecnica("AM5", 80, "DDR5"), new Precio(new BigDecimal("700000"), "COP"),
                Disponibilidad.DISPONIBLE, Vendedor.crear("Proveedor", "AUTORIZADO"), "SN-MB"));
        build.agregarComponente(Componente.crear("Fuente 100W", CategoriaComponente.PSU,
                new EspecificacionTecnica("ATX", 100, "DDR5"), new Precio(new BigDecimal("600000"), "COP"),
                Disponibilidad.DISPONIBLE, Vendedor.crear("Proveedor", "AUTORIZADO"), "SN-PSU"));

        assertThrows(ReglaDominioException.class, build::validarCompatibilidad);
    }

    @Test
    void buildConIncompatibilidadPorSocketDebeAdvertir() {
        Build build = Build.crear();
        build.agregarComponente(Componente.crear("Ryzen 7 7800X3D", CategoriaComponente.CPU,
                new EspecificacionTecnica("AM5", 120, "DDR5"), new Precio(new BigDecimal("800000"), "COP"),
                Disponibilidad.DISPONIBLE, Vendedor.crear("Proveedor", "AUTORIZADO"), "SN-CPU"));
        build.agregarComponente(Componente.crear("Intel Z690", CategoriaComponente.MOTHERBOARD,
                new EspecificacionTecnica("LGA1700", 80, "DDR5"), new Precio(new BigDecimal("700000"), "COP"),
                Disponibilidad.DISPONIBLE, Vendedor.crear("Proveedor", "AUTORIZADO"), "SN-MB"));
        build.agregarComponente(Componente.crear("Corsair RM850", CategoriaComponente.PSU,
                new EspecificacionTecnica("ATX", 850, "DDR5"), new Precio(new BigDecimal("900000"), "COP"),
                Disponibilidad.DISPONIBLE, Vendedor.crear("Proveedor", "AUTORIZADO"), "SN-PSU"));

        assertThrows(ReglaDominioException.class, build::validarCompatibilidad);
    }

    @Test
    void vendedorParticularNoPuedePublicarEnPreventa() {
        Vendedor vendedor = Vendedor.crear("Comprador local", "PARTICULAR");
        Componente componente = Componente.crear(
                "GPU usada",
                CategoriaComponente.GPU,
                new EspecificacionTecnica("PCIe4", 300, "GDDR6"),
                new Precio(new BigDecimal("1500000"), "COP"),
                Disponibilidad.DISPONIBLE,
                vendedor,
                "SN-USED"
        );

        assertThrows(ReglaDominioException.class, () -> componente.pasarAPreventa(LocalDate.now().plusDays(7)));
    }

    @Test
    void rmaSobreComponenteUsadoSinGarantiaFalla() {
        Componente componente = Componente.crear(
                "RAM usada sin garantía",
                CategoriaComponente.RAM,
                new EspecificacionTecnica("AM5", 50, "DDR5"),
                new Precio(new BigDecimal("250000"), "COP"),
                Disponibilidad.DISPONIBLE,
                Vendedor.crear("Vendedor particular", "PARTICULAR"),
                "SN-USA" 
        );
        componente.marcarComoUsadoSinGarantia();

        assertThrows(ReglaDominioException.class,
                () -> componente.crearSolicitudRMA(LocalDate.now(), 12, LocalDate.now().minusDays(2)));
    }
}
