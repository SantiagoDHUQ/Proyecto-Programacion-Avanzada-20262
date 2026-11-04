package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Prueba de repositorio JPA para el agregado Compra.
 * 
 * - Usa @SpringBootTest para levantar TODO el contexto (entidades JPA, adaptadores, mappers, repositorios).
 * - Usa @Transactional para que al finalizar la prueba se haga rollback y no quede información en H2.
 * - Se inyecta el PUERTO (CompraRepository), no el adaptador. Así la prueba no sabe que por debajo es JPA.
 * - Verifica que el mapeo Entidad JPA <-> Dominio funciona correctamente (incluido PrecioEmbeddable).
 */
@SpringBootTest
@Transactional
class CompraRepositoryJpaTest {

    @Autowired
    private CompraRepository repository;

    @Test
    void deberiaGuardarYRecuperarUnaCompraConSuPrecioYEstado() {
        // Arrange
        // Se crea un precio unitario que quedará congelado en el ítem
        Precio precioUnitario = new Precio(new BigDecimal("850000"), "COP");

        // Se crea un ítem de compra con una unidad física concreta
        // El precio y el número de serie se congelan al momento de crear la compra
        ItemCompra item = new ItemCompra(
                UUID.randomUUID(),  // unidadId
                "cpu-ryzen5",       // componenteId
                "SN-CPU-0001",      // numeroSerie
                precioUnitario      // precioUnitario congelado
        );

        // Se crea la compra usando el método de fábrica del dominio
        // Compra.realizar() valida: debe tener ítems, no repetir unidades, calcula precioCongelado
        // y deja la compra en estado PENDIENTE
        Compra compra = Compra.realizar(
                "C-TEST-001",       // id de la compra
                UUID.randomUUID(),  // buildId
                "ana",              // compradorId
                List.of(item)       // lista de ítems
        );

        // Act
        // Se guarda la compra a través del puerto
        // El CompraRepositoryJpaAdapter convierte Compra -> CompraEntity, guarda con JpaRepository y vuelve a mapear
        repository.guardar(compra);

        // Se recupera la compra desde H2 usando el mismo puerto
        Compra recuperada = repository.buscarPorId("C-TEST-001")
                .orElseThrow(() -> new AssertionError("No se encontró la compra guardada"));

        // Assert
        // Se verifica que se recupera correctamente con todos sus datos
        assertNotNull(recuperada, "La compra recuperada no debe ser nula");
        assertEquals("C-TEST-001", recuperada.getId(), "El ID debe coincidir");
        assertEquals("ana", recuperada.getCompradorId(), "El compradorId debe coincidir");
        assertEquals(UUID.fromString(compra.getBuildId().toString()), recuperada.getBuildId(), "El buildId debe coincidir");
        assertEquals(1, recuperada.getItems().size(), "Debe tener exactamente un ítem");
        assertEquals("cpu-ryzen5", recuperada.getItems().get(0).componenteId(), "El componenteId del ítem debe coincidir");
        assertEquals("SN-CPU-0001", recuperada.getItems().get(0).numeroSerie(), "El número de serie debe coincidir");
        assertEquals(850000, recuperada.getPrecioCongelado().monto().intValue(), "El precio congelado debe conservarse");
        assertEquals("COP", recuperada.getPrecioCongelado().moneda(), "La moneda debe conservarse");
        assertEquals(EstadoCompra.PENDIENTE, recuperada.getEstado(), "La compra debe nacer en estado PENDIENTE");
    }

    @Test
    void deberiaGuardarYRecuperarCompraConVariosItems() {
        // Arrange
        // Se crean dos ítems con precios distintos (se sumarán para el precio congelado)
        ItemCompra itemCpu = new ItemCompra(UUID.randomUUID(), "cpu-ryzen5", "SN-CPU-0002", new Precio(new BigDecimal("850000"), "COP"));
        ItemCompra itemRam = new ItemCompra(UUID.randomUUID(), "ram-16gb", "SN-RAM-0002", new Precio(new BigDecimal("320000"), "COP"));

        // Se crea la compra con ambos ítems
        Compra compra = Compra.realizar("C-TEST-002", UUID.randomUUID(), "bruno", List.of(itemCpu, itemRam));

        // Act
        repository.guardar(compra);
        Compra recuperada = repository.buscarPorId("C-TEST-002").orElseThrow();

        // Assert
        // Se verifica que se guardaron ambos ítems y que el precio congelado es la suma
        assertNotNull(recuperada);
        assertEquals("C-TEST-002", recuperada.getId());
        assertEquals("bruno", recuperada.getCompradorId());
        assertEquals(2, recuperada.getItems().size(), "Debe tener dos ítems");
        // 850000 + 320000 = 1170000
        assertEquals(1170000, recuperada.getPrecioCongelado().monto().intValue(), "El precio congelado debe ser la suma de los ítems");
        assertEquals("COP", recuperada.getPrecioCongelado().moneda());
        assertEquals(EstadoCompra.PENDIENTE, recuperada.getEstado());
    }
}
