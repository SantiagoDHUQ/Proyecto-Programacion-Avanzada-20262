# Guía de estudio: Pruebas avanzadas del backend (CompuParts)

## Objetivo

Aplicar al proyecto **CompuParts** las pruebas avanzadas vistas: pruebas del repositorio JPA, pruebas de endpoints con seguridad (401/403/200), comparación entre `@DataJpaTest` y `@SpringBootTest`, pruebas de integración y pruebas de errores en el controlador junto con la verificación de que no se guarda cuando falla una regla de negocio.

## Conceptos clave

- **Prueba unitaria**: Prueba una sola clase. Todo lo externo se simula con Mockito. Rápida y enfocada.
- **Prueba de integración**: Prueba varias capas juntas (Controller -> UseCase -> Dominio -> Repositorio -> BD). Detecta errores entre capas.
- **Slice test**: Levanta solo una parte del contexto. `@WebMvcTest` solo capa web, `@DataJpaTest` solo capa de persistencia.
- **401 vs 403**: `401 Unauthorized` = No se sabe quién es (falta token o es inválido). `403 Forbidden` = Se sabe quién es, pero no tiene el rol/permisos necesarios.

## Dependencias necesarias

Para estas pruebas se requieren:

```groovy
testImplementation 'org.springframework.security:spring-security-test'
testImplementation 'org.springframework.boot:spring-boot-starter-data-jpa-test'
```

> En Spring Boot 4 los starters de test por tecnología fueron separados, por lo que `spring-boot-starter-data-jpa-test` es el indicado para pruebas de persistencia.

## Parte 0. Pruebas del repositorio JPA

### ¿Por qué probarlo así?

Probar el repositorio contra una base de datos real (H2 en memoria) nos permite verificar que:

- El mapeo entre entidades de dominio y entidades JPA funciona correctamente.
- Los `@Embeddable` como `PrecioEmbeddable` se guardan correctamente.
- Los `@Enumerated` como `EstadoCompra` se persisten como `STRING`.
- El adaptador JPA + mapper traducen correctamente entre `Compra` (dominio) y `CompraEntity` (JPA).
- Se respeta el puerto `CompraRepository`, sin acoplar la prueba al adaptador directamente.

### ¿Qué hace `@Transactional`?

Con `@Transactional` la transacción se abre al inicio de la prueba y se hace **rollback** al finalizar. Esto evita que los registros guardados queden en la base de datos para la siguiente prueba.

### Implementación (adaptada a CompuParts)

Crear en `src/test/java/org/uniquindio/proyectoavanzadacompuparts/infrastructure/persistence/jpa/` la clase `CompraRepositoryJpaTest.java`:

```java
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

@SpringBootTest
@Transactional
class CompraRepositoryJpaTest {

    @Autowired
    private CompraRepository repository;

    @Test
    void deberiaGuardarYRecuperarUnaCompraConSuPrecioYEstado() {
        // Arrange: Se crea un ItemCompra con precio congelado
        Precio precioUnitario = new Precio(new BigDecimal("850000"), "COP");
        ItemCompra item = new ItemCompra(UUID.randomUUID(), "cpu-ryzen5", "SN-CPU-0001", precioUnitario);

        // Arrange: Se crea la compra con sus ítems. Compra.realizar calcula el precio congelado y la deja en PENDIENTE
        Compra compra = Compra.realizar("C-TEST-001", UUID.randomUUID(), "ana", List.of(item));

        // Act: Se guarda y se recupera usando el puerto del dominio
        repository.guardar(compra);
        Compra recuperada = repository.buscarPorId("C-TEST-001").orElseThrow();

        // Assert: Se verifica que el mapeo JPA<->Dominio preserva los datos
        assertNotNull(recuperada);
        assertEquals("C-TEST-001", recuperada.getId());
        assertEquals("ana", recuperada.getCompradorId());
        assertEquals(1, recuperada.getItems().size());
        assertEquals(850000, recuperada.getPrecioCongelado().monto().intValue());
        assertEquals("COP", recuperada.getPrecioCongelado().moneda());
        assertEquals(EstadoCompra.PENDIENTE, recuperada.getEstado());
    }
}
```

**Nota:** Se inyecta `CompraRepository` (puerto) y no `CompraRepositoryJpaAdapter`. Así la prueba no depende de la implementación concreta, solo del contrato.

## Parte 1. Probar un endpoint con seguridad (401, 403 y 200)

### ¿Qué investigar?

- **spring-security-test**: Permite simular autenticación/autorización en `MockMvc` sin necesidad de generar un JWT real.
- **`jwt().authorities(...)`**: Simula un usuario autenticado con sus autoridades/roles. En CompuParts se trabaja con roles como `COMPRADOR`, `VENDEDOR`, `ADMIN`.
- **`@SpringBootTest + @AutoConfigureMockMvc` vs `@WebMvcTest`**: `@WebMvcTest` solo levanta la capa web y **no carga** la `SecurityFilterChain` real. Para probar las reglas reales de seguridad (incluida la configuración OAuth2 Resource Server) hay que levantar el contexto completo con `@SpringBootTest`.

### Implementación (adaptada a CompuParts)

Crear en `src/test/java/org/uniquindio/proyectoavanzadacompuparts/infrastructure/rest/` `SeguridadEndpointTest.java`:

```java
package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sinTokenDeberiaResponder401() throws Exception {
        // Sin ningún tipo de autenticación, la petición es rechazada por el Resource Server
        mockMvc.perform(get("/api/componentes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conRolIncorrectoDeberiaResponder403() throws Exception {
        // Ejemplo: si POST /api/componentes solo permite ciertos roles y el usuario es COMPRADOR
        // se espera 403 (autenticado, pero sin autorización)
        mockMvc.perform(post("/api/componentes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("COMPRADOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void conRolCorrectoYPeticionValidaDeberiaResponder() throws Exception {
        // Con un rol permitido se deja pasar el filtro de seguridad
        // (puede devolver 400 por validación del body, pero NO 401 ni 403)
        mockMvc.perform(get("/api/componentes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("VENDEDOR")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
```

**Aprendizaje:** Con `jwt()` simulamos el token sin pasar por `/api/auth/login`. Las autoridades deben coincidir con las que interpreta nuestro `JwtAuthenticationConverter`.

## Parte 2. `@DataJpaTest` contra `@SpringBootTest`

### Diferencias clave

| Aspecto | `@DataJpaTest` | `@SpringBootTest` |
| --- | --- | --- |
| **Alcance** | Solo capa de persistencia (entidades, repositorios JPA, `EntityManager`) | Todo el contexto de la aplicación (REST, use cases, servicios, configuración, seguridad) |
| **Velocidad** | Más rápido | Más lento |
| **Adaptadores propios** | **NO** se cargan. Hay que importarlos con `@Import` | Se cargan todos los beans |
| **Transaccionalidad** | Por defecto `@Transactional` con rollback al finalizar | Depende, muchas veces se añade explícitamente |
| **Uso ideal** | Probar repositorios JPA y consultas personalizadas | Pruebas de integración end-to-end o con seguridad real |

### Implementación (adaptada a CompuParts)

En Spring Boot 4 el paquete es `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`.

```java
package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.adapter.VendedorRepositoryJpaAdapter;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.mapper.VendedorEntityMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({VendedorRepositoryJpaAdapter.class, VendedorEntityMapper.class})
class VendedorRepositoryDataJpaTest {

    @Autowired
    private VendedorRepository repository;

    @Test
    void deberiaListarVendedoresDesdeH2() {
        // Act: Al cargar el contexto con DataJpaTest + data.sql se cargan datos semilla
        var vendedores = repository.listarTodos();

        // Assert: Se comprueba que se pueden leer desde la BD embebida
        assertTrue(vendedores.size() >= 1);
    }
}
```

**Lección aprendida:** `@DataJpaTest` es ideal para aislar la persistencia. Como usamos adaptadores propios (`*RepositoryJpaAdapter`), debemos importarlos explícitamente junto a sus mappers para que funcionen.

## Parte 3. Pruebas de integración

### ¿Qué es?

Una **prueba de integración** valida el flujo completo real sin mocks. Se prueba desde el endpoint HTTP hasta la base de datos. Si pasa, confirma que todas las piezas están correctamente conectadas.

### ¿Cuándo usarla?

- Para validar flujos críticos del negocio (Realizar compra, Confirmar compra, Solicitar RMA).
- Para detectar errores de configuración (mapeos, validaciones, transacciones).
- No es necesario hacer una por cada caso borde. Con 1-2 flujos representativos basta.

### Implementación (adaptada a CompuParts)

```java
package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ComprarDesdeBuildIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deberiaRealizarCompraDesdeBuildYQuedarPendiente() throws Exception {
        // Usando datos presentes en data.sql (builds/componentes/unidades disponibles)
        String cuerpo = """
                {
                  "buildId": "b1111111-1111-1111-1111-111111111111",
                  "compradorId": "ana"
                }
                """;

        mockMvc.perform(post("/api/compras")
                        .with(jwt().jwt(j -> j.claim("uid", "ana"))
                                .authorities(new SimpleGrantedAuthority("COMPRADOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.compradorId").value("ana"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.precioCongelado").exists());
    }
}
```

**Importante:** Al usar `@Transactional` se hace rollback al finalizar, por lo que las reservas/unidades cambiadas durante la prueba no afectan otras pruebas.

## Parte 4. Más casos en el controller + verificación "no guardar si falla"

### A) Casos de error en el controlador con `@WebMvcTest`

El objetivo es cubrir: **404 (recurso no encontrado), 400 (regla de negocio rota) y validación con datos inválidos (sin llamar al caso de uso)**.

Para esto usamos `@MockitoBean` para simular el caso de uso y `@RestControllerAdvice` para mapear excepciones a códigos HTTP.

```java
package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RealizarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompraController.class)
@AutoConfigureMockMvc(addFilters = false)
class CompraControllerErroresTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RealizarCompraUseCase realizarCompraUseCase;

    @Test
    void deberiaResponder404CuandoElBuildNoExiste() throws Exception {
        // Simulamos que el caso de uso lanza excepción cuando no encuentra el recurso
        when(realizarCompraUseCase.ejecutar(any()))
                .thenThrow(new RecursoNoEncontradoException("No existe el Build"));

        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "buildId": "00000000-0000-0000-0000-000000000000", "compradorId": "ana" }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deberiaResponder400CuandoSeViolaUnaReglaDeDominio() throws Exception {
        // Simulamos una violación de invariante del dominio
        when(realizarCompraUseCase.ejecutar(any()))
                .thenThrow(new ReglaDominioException("El Build debe estar listo para compra"));

        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "buildId": "b1111111-1111-1111-1111-111111111111", "compradorId": "ana" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void noDeberiaLlamarAlCasoDeUsoSiLosDatosSonInvalidos() throws Exception {
        // Enviamos JSON vacío -> falla validación Bean Validation -> Controller Advice responde 400
        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        // Verificamos que NUNCA se llamó al caso de uso
        verify(realizarCompraUseCase, never()).ejecutar(any());
    }
}
```

> `@AutoConfigureMockMvc(addFilters = false)` desactiva los filtros de seguridad solo para este test enfocado al controlador. La seguridad real se prueba en la **Parte 1**.

### B) Verificar que no se guarda si algo falla (prueba unitaria del caso de uso)

En el caso de uso se debe demostrar que, si falla una validación previa, **no se llama al repositorio para guardar**. Esto garantiza que no se persiste información inconsistente.

```java
package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CompraService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealizarCompraUseCaseTest {

    @Mock
    private BuildRepository buildRepository;

    @Mock
    private ComponenteRepository componenteRepository;

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private CompraService compraService;

    @InjectMocks
    private RealizarCompraUseCase realizarCompraUseCase;

    @Test
    void noDeberiaGuardarSiElBuildNoExiste() {
        // Arrange: Simulamos que el build no existe
        UUID buildId = UUID.randomUUID();
        when(buildRepository.buscarPorId(buildId)).thenReturn(Optional.empty());

        // Act & Assert: Se espera excepción y NO se debe guardar nada
        assertThrows(RecursoNoEncontradoException.class,
                () -> realizarCompraUseCase.ejecutar(buildId, "ana"));

        // Verify: Se confirma que compraRepository.guardar() nunca fue invocado
        verify(compraRepository, never()).guardar(any());
    }
}
```

## Conclusión

- **Repositorio**: Probar con `@SpringBootTest + @Transactional` contra H2 valida el mapeo JPA real y el uso correcto de los puertos.
- **Seguridad**: Usar `@SpringBootTest + @AutoConfigureMockMvc + jwt()` nos permite probar 401, 403 y 200 con la configuración real.
- **`@DataJpaTest` vs `@SpringBootTest`**: `@DataJpaTest` es más liviano y perfecto para persistencia, pero requiere `@Import` para nuestros adaptadores. `@SpringBootTest` levanta todo y es ideal para integración con seguridad.
- **Integración**: Las pruebas end-to-end confirman que el flujo completo funciona correctamente.
- **Controlador + robustez**: Probar errores con `@WebMvcTest` y `verify(never())` garantiza que **no se persiste información** cuando hay validación fallida o regla de negocio rota. Esto es fundamental para mantener la integridad de CompuParts.


## Entregable: Reflexión por partes (Parte 1, 2 y 3)

### Parte 1. Probar un endpoint con seguridad (401, 403 y 200)

Para probar la seguridad real de CompuParts usamos `@SpringBootTest` junto a `@AutoConfigureMockMvc` y `spring-security-test`. Con `jwt().authorities(...)` simulamos un usuario autenticado con su rol sin tener que hacer login, lo cual nos permite verificar que las reglas del `SecurityFilterChain` se aplican correctamente. Lo importante fue entender la diferencia entre `401` (no autenticado) y `403` (autenticado pero sin permisos). Lo que más nos costó fue darnos cuenta de que si usábamos `@WebMvcTest` la seguridad real no se cargaba y los resultados eran distintos a los esperados.

### Parte 2. `@DataJpaTest` contra `@SpringBootTest`

`@DataJpaTest` levanta únicamente la capa de persistencia (entidades, repositorios JPA, `EntityManager`), mientras que `@SpringBootTest` levanta todo el contexto de la aplicación. Para nuestros adaptadores JPA propios tuvimos que usar `@Import` junto con los `*RepositoryJpaAdapter` y sus `*EntityMapper`, ya que `@DataJpaTest` no los carga automáticamente. En cambio con `@SpringBootTest` todo funciona sin necesidad de importarlos. Lo que más nos costó fue entender ese detalle y corregir los imports al migrar a Spring Boot 4, ya que el paquete de `@DataJpaTest` cambió.

### Parte 3. Pruebas de integración

Una prueba de integración recorre todo el flujo real: petición HTTP -> Controller -> UseCase -> Dominio -> Repositorio JPA -> H2, sin usar mocks. Con `@SpringBootTest`, `@AutoConfigureMockMvc` y `@Transactional` logramos probar que todas las capas se conectan correctamente y que, al hacer rollback, no ensuciamos la base entre pruebas. Nos dimos cuenta de que estas pruebas son más lentas, pero son las únicas que realmente nos dicen si el flujo completo funciona. Lo que más nos costó fue armar el JSON correcto con los datos que existen en `data.sql` para que el flujo fuera válido.
