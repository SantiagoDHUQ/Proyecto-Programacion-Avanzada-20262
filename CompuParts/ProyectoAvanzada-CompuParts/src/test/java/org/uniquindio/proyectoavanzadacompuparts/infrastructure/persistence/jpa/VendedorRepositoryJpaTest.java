package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de repositorio JPA para el agregado Vendedor.
 * 
 * Verifica el guardado, recuperación, listado y eliminación usando el adaptador JPA
 * a través del puerto del dominio. Se usa @SpringBootTest + @Transactional para
 * probar contra H2 con rollback automático.
 */
@SpringBootTest
@Transactional
class VendedorRepositoryJpaTest {

    @Autowired
    private VendedorRepository repository;

    @Test
    void deberiaGuardarYRecuperarUnVendedor() {
        // Arrange
        // Se crea un vendedor autorizado usando el método de fábrica del dominio
        Vendedor vendedor = Vendedor.crear("v-test-1", "Tienda Prueba", "AUTORIZADO");

        // Act
        // Guardar y recuperar usando el puerto
        repository.guardar(vendedor);
        Optional<Vendedor> recuperado = repository.buscarPorId("v-test-1");

        // Assert
        assertTrue(recuperado.isPresent(), "El vendedor debe existir tras guardarlo");
        assertEquals("v-test-1", recuperado.get().getId());
        assertEquals("Tienda Prueba", recuperado.get().getNombre());
        assertEquals(Vendedor.TipoVendedor.AUTORIZADO, recuperado.get().getTipo());
        assertTrue(recuperado.get().esAutorizado());
    }

    @Test
    void deberiaListarTodosLosVendedores() {
        // Arrange
        // Se guardan dos vendedores distintos
        Vendedor vendedor1 = Vendedor.crear("v-test-2", "Vendedor Uno", "AUTORIZADO");
        Vendedor vendedor2 = Vendedor.crear("v-test-3", "Vendedor Dos", "PARTICULAR");

        repository.guardar(vendedor1);
        repository.guardar(vendedor2);

        // Act
        var lista = repository.listarTodos();

        // Assert
        assertNotNull(lista);
        assertTrue(lista.size() >= 2, "Debe contener al menos los dos vendedores guardados");
    }

    @Test
    void deberiaEliminarUnVendedor() {
        // Arrange
        Vendedor vendedor = Vendedor.crear("v-test-4", "Vendedor A Eliminar", "PARTICULAR");
        repository.guardar(vendedor);

        // Verificamos que existe antes de eliminar
        assertTrue(repository.buscarPorId("v-test-4").isPresent());

        // Act
        repository.eliminar("v-test-4");

        // Assert
        assertTrue(repository.buscarPorId("v-test-4").isEmpty(), "El vendedor debe desaparecer tras eliminarlo");
    }
}
