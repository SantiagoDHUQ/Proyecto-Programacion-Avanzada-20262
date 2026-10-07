package org.uniquindio.proyectoavanzadacompuparts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Pedido;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Direccion;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Disponibilidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoPedido;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DominioYAplicacionTests {

        /**
         * Prueba 1 (Value Object): Igualdad por valor.
         * Dos precios con idéntico monto y moneda son matemáticamente el mismo objeto
         * en DDD.
         */
        @Test
        void precioDosObjetosConMismoMontoYMonedaSonIguales() {
                // Arrange
                Precio p1 = new Precio(new BigDecimal("15000"), "COP");
                Precio p2 = new Precio(new BigDecimal("15000"), "COP");
                // Act + Assert
                assertEquals(p1, p2); // Igualdad por VALOR
        }

                /**
                 * Prueba 2 (Value Object): Validación que lanza ReglaDominioException.
                 * Prohíbe la existencia de precios negativos.
                 */
        @Test
        void precioRechazaMontoNegativoYFalla() {
                // Arrange
                Executable creacionPrecio = () -> new Precio(new BigDecimal("-1.00"), "COP");
                // Act + Assert
                assertThrows(ReglaDominioException.class, creacionPrecio);
        }

        /**
         * Prueba 3 (Entidad): Igualdad por IDENTIDAD.
         * Aunque tengan datos diferentes, si comparten el mismo UUID, son la misma
         * entidad.
         */
        @Test
        void vendedorDosObjetosConMismoIdSonIguales() {
                // Arrange
                java.util.UUID idCompartido = java.util.UUID.randomUUID();
                Vendedor original = Vendedor.reconstituir(idCompartido, "Vendedor Oficial", "AUTORIZADO");
                Vendedor otro = Vendedor.reconstituir(idCompartido, "Vendedor Cambiado", "PARTICULAR");
                // Act + Assert
                assertEquals(original, otro); // Igualdad por IDENTIDAD
        }

        /**
         * Prueba 4 (Entidad): Una regla que se protege.
         * Valida que la entidad Build reconozca que un ensamble solo está "completo"
         * cuando contiene Procesador, Tarjeta Madre y Fuente de Poder.
         */
        @Test
        void buildEsCompletoCuandoTieneMotherboardCpuYPsu() {
                // Arrange
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

                // Act + Assert
                assertTrue(build.esCompleto()); // Regla protegida exitosamente
        }

        /**
         * Prueba 5 (Invariante Build): Excepción esperada Y estado intacto.
         * Un build incompleto falla al marcarse como listo para compra y se queda
         * EN_CONSTRUCCION.
         */
        @Test
        void buildMarcarListoFallaSiNoEstaCompletoYNoCambiaEstado() {
                // Arrange
                Build build = Build.crear();
                // Solo agregamos CPU (incompleto)
                build.agregarComponente(Componente.crear("Ryzen", CategoriaComponente.CPU,
                                new EspecificacionTecnica("AM5", 120, "DDR5"),
                                new Precio(new BigDecimal("800000"), "COP"),
                                Disponibilidad.DISPONIBLE, Vendedor.crear("Prov", "AUTORIZADO"), "SN-CPU"));

                assertEquals(EstadoBuild.EN_CONSTRUCCION, build.getEstado()); // Estado inicial

                // Act + Assert (la excepción)
                assertThrows(ReglaDominioException.class, build::marcarComoListoParaCompra);

                // Assert (el estado)
                assertEquals(EstadoBuild.EN_CONSTRUCCION, build.getEstado()); // Estado intacto tras rechazo
        }

        /**
         * Prueba 6 (Invariante Build): Excepción esperada Y estado intacto.
         * La compatibilidad falla si la PSU es insuficiente para el consumo total.
         */
             /**
         * Prueba 6 (Invariante Build): Excepción esperada Y estado INCOMPATIBLE.
         * La compatibilidad falla si la PSU es insuficiente para el consumo total,
         * y el Build queda marcado como INCOMPATIBLE.
         */
        @Test
        void buildValidarCompatibilidadFallaPorConsumoYMarcaIncompatible() {
                // Arrange
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

                // Act + Assert (la excepción)
                assertThrows(ReglaDominioException.class, build::validarCompatibilidad);

                // Assert (el estado)
                assertEquals(EstadoBuild.INCOMPATIBLE, build.getEstado()); // Queda marcado como incompatible
        }


        /**
         * Prueba 7 (Invariante Componente): Excepción esperada Y estado intacto.
         * Pasar a preventa sin una fecha estimada es ilegal.
         */
        @Test
        void componentePasarPreventaFallaSinFechaYNoCambiaEstado() {
                // Arrange
                Componente componente = Componente.crear("Ryzen", CategoriaComponente.CPU,
                                new EspecificacionTecnica("AM5", 120, "DDR5"),
                                new Precio(new BigDecimal("800000"), "COP"),
                                Disponibilidad.DISPONIBLE, Vendedor.crear("Vendedor", "AUTORIZADO"), "SN-001");

                assertEquals(Disponibilidad.DISPONIBLE, componente.getDisponibilidad());

                // Act + Assert (la excepción)
                assertThrows(ReglaDominioException.class, () -> componente.pasarAPreventa(null));

                // Assert (el estado)
                assertEquals(Disponibilidad.DISPONIBLE, componente.getDisponibilidad()); // Estado intacto
        }

        /**
         * Prueba 8 (Invariante Componente): Excepción esperada Y estado intacto.
         * Un vendedor particular no puede lanzar preventas.
         */
        @Test
        void componentePasarPreventaFallaSiVendedorParticularYNoCambiaEstado() {
                // Arrange
                Componente componente = Componente.crear("RAM", CategoriaComponente.RAM,
                                new EspecificacionTecnica("AM5", 50, "DDR5"),
                                new Precio(new BigDecimal("250000"), "COP"),
                                Disponibilidad.DISPONIBLE, Vendedor.crear("Particular", "PARTICULAR"), "SN-USA");

                assertEquals(Disponibilidad.DISPONIBLE, componente.getDisponibilidad());

                // Act + Assert (la excepción)
                assertThrows(ReglaDominioException.class, () -> componente.pasarAPreventa(LocalDate.now().plusDays(7)));

                // Assert (el estado)
                assertEquals(Disponibilidad.DISPONIBLE, componente.getDisponibilidad()); // Estado intacto
        }

        /**
         * Prueba 9 (Invariante Pedido): Excepción esperada Y estado intacto.
         * Pagar un pedido sin ítems falla y no cambia de estado.
         */
        @Test
        void pedidoConfirmarPagoFallaSinItemsYNoCambiaEstado() {
                // Arrange
                Direccion direccion = new Direccion("Calle 1", "Ciudad", "00000", "Pais");
                Pedido pedido = Pedido.crear("cliente-123", direccion);
                assertEquals(EstadoPedido.CREADO, pedido.getEstado());

                // Act + Assert
                assertThrows(ReglaDominioException.class, pedido::confirmarPago);

                // Assert
                assertEquals(EstadoPedido.CREADO, pedido.getEstado());
        }

        /**
         * Prueba 10 (Invariante Pedido): Excepción esperada Y estado intacto.
         * Modificar un pedido ya pagado falla y no cambia ítems.
         */
        @Test
        void pedidoAgregarItemFallaSiEstaPagadoYNoCambiaItems() {
                // Arrange
                Direccion direccion = new Direccion("Calle 1", "Ciudad", "00000", "Pais");
                Pedido pedido = Pedido.crear("cliente-123", direccion);
                pedido.agregarItem("prod-1", 1, new Precio(new BigDecimal("100"), "COP"));
                pedido.confirmarPago();
                
                assertEquals(EstadoPedido.PAGADO, pedido.getEstado());
                int cantidadItems = pedido.getItems().size();

                // Act + Assert
                assertThrows(ReglaDominioException.class, () -> pedido.agregarItem("prod-2", 2, new Precio(new BigDecimal("200"), "COP")));

                // Assert
                assertEquals(EstadoPedido.PAGADO, pedido.getEstado());
                assertEquals(cantidadItems, pedido.getItems().size());
        }
}