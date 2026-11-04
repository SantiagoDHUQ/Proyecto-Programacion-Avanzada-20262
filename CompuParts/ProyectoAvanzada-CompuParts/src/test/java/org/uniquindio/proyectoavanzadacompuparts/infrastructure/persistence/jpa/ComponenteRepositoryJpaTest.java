package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de repositorio JPA para el agregado Componente con sus UnidadesInventario.
 * 
 * Verifica que:
 * - Se guarda un Componente con sus Value Objects (Precio, EspecificacionTecnica, Garantia)
 * - Se persisten las UnidadesInventario con cascade ALL (agregadas al componente)
 * - El mapeo embeddable funciona correctamente
 * - Se puede recuperar el agregado completo desde la BD
 */
@SpringBootTest
@Transactional
class ComponenteRepositoryJpaTest {

    @Autowired
    private ComponenteRepository componenteRepository;

    @Autowired
    private VendedorRepository vendedorRepository;

    @Test
    void deberiaGuardarYRecuperarComponenteConUnidades() {
        // Arrange
        // Primero se guarda el vendedor (necesario por integridad)
        Vendedor vendedor = Vendedor.crear("v-test-comp", "Proveedor Test", "AUTORIZADO");
        vendedorRepository.guardar(vendedor);

        // Se crean los Value Objects del componente
        EspecificacionTecnica esp = new EspecificacionTecnica("AM4", 65, null);
        Precio precio = new Precio(new BigDecimal("850000"), "COP");

        // Se crea el componente con garantía (obligatoria para vendedor AUTORIZADO)
        Componente componente = Componente.crear("cpu-test-jpa", "AMD Ryzen Test", CategoriaComponente.CPU,
                esp, precio, vendedor, 36);

        // Se agregan unidades físicas al componente
        componente.agregarUnidad("v-test-comp", "SN-JPA-001", false);
        componente.agregarUnidad("v-test-comp", "SN-JPA-002", false);

        // Act
        // Se guarda el componente completo (con sus unidades gracias a CascadeType.ALL)
        componenteRepository.guardar(componente);

        // Se recupera el componente desde BD
        Optional<Componente> recuperado = componenteRepository.buscarPorId("cpu-test-jpa");

        // Assert
        assertTrue(recuperado.isPresent(), "El componente debe existir tras guardarlo");
        Componente comp = recuperado.get();

        assertEquals("cpu-test-jpa", comp.getId());
        assertEquals("AMD Ryzen Test", comp.getNombre());
        assertEquals(CategoriaComponente.CPU, comp.getCategoria());
        assertEquals("AM4", comp.getEspecificacion().socket());
        assertEquals(65, comp.getEspecificacion().wattajeRequerido());
        assertEquals(850000, comp.getPrecio().monto().intValue());
        assertEquals("COP", comp.getPrecio().moneda());
        assertNotNull(comp.getVendedor());
        assertEquals("v-test-comp", comp.getVendedor().getId());
        assertTrue(comp.getGarantia().isPresent(), "Debe tener garantía por ser vendedor AUTORIZADO");
        assertEquals(36, comp.getGarantia().get().dias());
        assertEquals(2, comp.getUnidades().size(), "Deben recuperarse las 2 unidades guardadas");
        assertEquals(2, comp.stockDisponible(), "Deben haber 2 unidades disponibles");
    }

    @Test
    void debeVerificarExistenciaDeNumeroSerie() {
        // Arrange
        Vendedor vendedor = Vendedor.crear("v-test-comp2", "Proveedor Test 2", "AUTORIZADO");
        vendedorRepository.guardar(vendedor);

        EspecificacionTecnica esp = new EspecificacionTecnica("AM4", 65, null);
        Precio precio = new Precio(new BigDecimal("850000"), "COP");
        Componente componente = Componente.crear("cpu-test-jpa2", "AMD Ryzen Test 2", CategoriaComponente.CPU,
                esp, precio, vendedor, 36);
        componente.agregarUnidad("v-test-comp2", "SN-UNICO-999", false);
        componenteRepository.guardar(componente);

        // Act & Assert
        assertTrue(componenteRepository.existeNumeroSerie("SN-UNICO-999"), "Debe encontrar el número de serie");
        assertTrue(componenteRepository.existeNumeroSerie("sn-unico-999"), "La búsqueda debe ser case insensitive");
    }
}
