package org.uniquindio.proyectoavanzadacompuparts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Disponibilidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DominioYAplicacionTests {

        // =========================================================================
        // 1. PRUEBAS DE VALUE OBJECT (Mínimo 2)
        // =========================================================================

        /**
         * Prueba 1 (Value Object): Igualdad por valor.
         * Dos precios con idéntico monto y moneda son matemáticamente el mismo objeto
         * en DDD.
         */
        @Test
        void precioDosObjetosConMismoMontoYMonedaSonIguales() {
                Precio p1 = new Precio(new BigDecimal("15000"), "COP");
                Precio p2 = new Precio(new BigDecimal("15000"), "COP");
                assertEquals(p1, p2); // Igualdad por VALOR
        }

                /**
                 * Prueba 2 (Value Object): Validación que lanza ReglaDominioException.
                 * Prohíbe la existencia de precios negativos.
                 */
        @Test
        void precioRechazaMontoNegativoYFalla() {
                Executable creacionPrecio = () -> new Precio(new BigDecimal("-1.00"), "COP");
                assertThrows(ReglaDominioException.class, creacionPrecio);
        }

        // =========================================================================
        // 2. PRUEBAS DE ENTIDAD (Mínimo 2)
        // =========================================================================

        /**
         * Prueba 3 (Entidad): Igualdad por IDENTIDAD.
         * Aunque tengan datos diferentes, si comparten el mismo UUID, son la misma
         * entidad.
         */
        @Test
        void vendedorDosObjetosConMismoIdSonIguales() {
                java.util.UUID idCompartido = java.util.UUID.randomUUID();
                Vendedor original = Vendedor.reconstituir(idCompartido, "Vendedor Oficial", "AUTORIZADO");
                Vendedor otro = Vendedor.reconstituir(idCompartido, "Vendedor Cambiado", "PARTICULAR");
                assertEquals(original, otro); // Igualdad por IDENTIDAD
        }

        /**
         * Prueba 4 (Entidad): Una regla que se protege.
         * Valida que la entidad Build reconozca que un ensamble solo está "completo"
         * cuando contiene Procesador, Tarjeta Madre y Fuente de Poder.
         */
        @Test
        void buildEsCompletoCuandoTieneMotherboardCpuYPsu() {
                Build build = Build.crear();
                build.agregarComponente(Componente.crear("Ryzen", CategoriaComponente.CPU,
                                new EspecificacionTecnica("AM5", 120, "DDR5"),
                                new Precio(new BigDecimal("800000"), "COP"),
                                Disponibilidad.DISPONIBLE, Vendedor.crear("Proveedor", "AUTORIZADO"), "SN-CPU"));
                build.agregarComponente(Componente.crear("MSI", CategoriaComponente.MOTHERBOARD,
                                new EspecificacionTecnica("AM5", 80, "DDR5"),
                                new Precio(new BigDecimal("700000"), "COP"),
                                Disponibilidad.DISPONIBLE, Vendedor.crear("Proveedor", "AUTORIZADO"), "SN-MB"));
                build.agregarComponente(Componente.crear("Corsair", CategoriaComponente.PSU,
                                new EspecificacionTecnica("ATX", 850, "DDR5"),
                                new Precio(new BigDecimal("900000"), "COP"),
                                Disponibilidad.DISPONIBLE, Vendedor.crear("Proveedor", "AUTORIZADO"), "SN-PSU"));

                assertTrue(build.esCompleto()); // Regla protegida exitosamente
        }

        // =========================================================================
        // 3. PRUEBAS DE AGREGADO / INVARIANTES (Mínimo 2 por Agregado = 4 en total)
        // =========================================================================

        // --- AGREGADO 1: BUILD ---

        /**
         * Prueba 5 (Invariante Build): Excepción esperada Y estado intacto.
         * Un build incompleto falla al marcarse como listo para compra y se queda
         * EN_CONSTRUCCION.
         */
        @Test
        void buildMarcarListoFallaSiNoEstaCompletoYNoCambiaEstado() {
                Build build = Build.crear();
                // Solo agregamos CPU (incompleto)
                build.agregarComponente(Componente.crear("Ryzen", CategoriaComponente.CPU,
                                new EspecificacionTecnica("AM5", 120, "DDR5"),
                                new Precio(new BigDecimal("800000"), "COP"),
                                Disponibilidad.DISPONIBLE, Vendedor.crear("Prov", "AUTORIZADO"), "SN-CPU"));

                assertEquals(EstadoBuild.EN_CONSTRUCCION, build.getEstado()); // Estado inicial

                assertThrows(ReglaDominioException.class, build::marcarComoListoParaCompra);

                assertEquals(EstadoBuild.EN_CONSTRUCCION, build.getEstado()); // Estado intacto tras rechazo
        }

        /**
         * Prueba 6 (Invariante Build): Excepción esperada Y estado intacto.
         * La compatibilidad falla si la PSU es insuficiente para el consumo total.
         */
        @Test
        void buildValidarCompatibilidadFallaPorConsumoYNoCambiaEstado() {
                Build build = Build.crear();
                build.agregarComponente(Componente.crear("Ryzen", CategoriaComponente.CPU,
                                new EspecificacionTecnica("AM5", 120, "DDR5"),
                                new Precio(new BigDecimal("800000"), "COP"),
                                Disponibilidad.DISPONIBLE, Vendedor.crear("Prov", "AUTORIZADO"), "SN-CPU"));
                build.agregarComponente(Componente.crear("Fuente 100W", CategoriaComponente.PSU,
                                new EspecificacionTecnica("ATX", 100, "DDR5"),
                                new Precio(new BigDecimal("600000"), "COP"),
                                Disponibilidad.DISPONIBLE, Vendedor.crear("Prov", "AUTORIZADO"), "SN-PSU"));

                assertEquals(EstadoBuild.EN_CONSTRUCCION, build.getEstado()); // Estado inicial

                assertThrows(ReglaDominioException.class, build::validarCompatibilidad);

                assertEquals(EstadoBuild.EN_CONSTRUCCION, build.getEstado()); // Estado intacto tras rechazo
        }

        // --- AGREGADO 2: COMPONENTE ---

        /**
         * Prueba 7 (Invariante Componente): Excepción esperada Y estado intacto.
         * Pasar a preventa sin una fecha estimada es ilegal.
         */
        @Test
        void componentePasarPreventaFallaSinFechaYNoCambiaEstado() {
                Componente componente = Componente.crear("Ryzen", CategoriaComponente.CPU,
                                new EspecificacionTecnica("AM5", 120, "DDR5"),
                                new Precio(new BigDecimal("800000"), "COP"),
                                Disponibilidad.DISPONIBLE, Vendedor.crear("Vendedor", "AUTORIZADO"), "SN-001");

                assertEquals(Disponibilidad.DISPONIBLE, componente.getDisponibilidad());

                assertThrows(ReglaDominioException.class, () -> componente.pasarAPreventa(null));

                assertEquals(Disponibilidad.DISPONIBLE, componente.getDisponibilidad()); // Estado intacto
        }

        /**
         * Prueba 8 (Invariante Componente): Excepción esperada Y estado intacto.
         * Un vendedor particular no puede lanzar preventas.
         */
        @Test
        void componentePasarPreventaFallaSiVendedorParticularYNoCambiaEstado() {
                Componente componente = Componente.crear("RAM", CategoriaComponente.RAM,
                                new EspecificacionTecnica("AM5", 50, "DDR5"),
                                new Precio(new BigDecimal("250000"), "COP"),
                                Disponibilidad.DISPONIBLE, Vendedor.crear("Particular", "PARTICULAR"), "SN-USA");

                assertEquals(Disponibilidad.DISPONIBLE, componente.getDisponibilidad());

                assertThrows(ReglaDominioException.class, () -> componente.pasarAPreventa(LocalDate.now().plusDays(7)));

                assertEquals(Disponibilidad.DISPONIBLE, componente.getDisponibilidad()); // Estado intacto
        }
}
