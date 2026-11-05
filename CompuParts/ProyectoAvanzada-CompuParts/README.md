# ProyectoAvanzada-CompuParts

Sistema de gestión para una tienda de componentes de PC con arquitectura hexagonal (Domain-Driven Design). Permite gestionar vendedores, componentes con su inventario, builds (armados), compras, preventas y solicitudes RMA, con persistencia en JPA + H2.

## Tecnologías

- Java 21
- Spring Boot 4.1.1
- Spring Data JPA / Hibernate
- H2 Database (en memoria)
- Spring Security + OAuth2 Resource Server
- Gradle
- Lombok
- SpringDoc OpenAPI

## Arquitectura

El proyecto sigue **Arquitectura Hexagonal (Puertos y Adaptadores)** con enfoque **DDD**. El dominio es completamente independiente de frameworks, mientras que la infraestructura se encarga de conectarlo con el exterior (REST, persistencia, etc.).

```
src/main/java/org/uniquindio/proyectoavanzadacompuparts/
├── application/        # Casos de uso y orquestación
├── domain/             # Núcleo del negocio (entidades, VOs, reglas)
└── infrastructure/     # Adaptadores (REST, JPA, configuración)
```

## Explicación por paquetes

### `domain/`

El corazón de la aplicación. No tiene dependencias de Spring, JPA u otros frameworks.

| Paquete | Descripción |
| --- | --- |
| `entity/` | **Agregados y entidades**. Contienen las reglas de negocio e invariantes. Ej: `Componente`, `Build`, `Compra`, `Vendedor`, `UnidadInventario`, `ReservaPreventa`, `SolicitudRMA`. |
| `valueobject/` | **Objetos de Valor (VO)**. Inmutables, se identifican por sus atributos. Ej: `Precio`, `EspecificacionTecnica`, `Garantia`, `ItemCompra`, `EstadoCompra`, `EstadoBuild`, `CategoriaComponente`. |
| `repository/` | **Puertos de persistencia**. Interfaces que definen cómo se accede a los agregados sin depender de la implementación. Son implementados por los adaptadores de infraestructura. |
| `factory/` | **Fábricas**. Encargadas de crear agregados respetando sus reglas de construcción (`VendedorFactory`, `ComponenteFactory`, `BuildFactory`). |
| `service/` | **Servicios de Dominio**. Lógica que involucra múltiples agregados o reglas complejas que no pertenecen a una sola entidad (`CompraService`, `RmaService`, `PreventaService`, `CatalogoService`, etc.). |
| `exception/` | **Excepciones de dominio**. `ReglaDominioException` para violaciones de invariantes. |

### `application/`

Capa de aplicación que orquesta el flujo entre dominio e infraestructura. Los casos de uso son delgados: validan, coordinan agregados y delegan reglas al dominio.

| Paquete | Descripción |
| --- | --- |
| `usecase/` | **Casos de uso**. Un caso de uso por acción del negocio (ej: `RegistrarVendedorUseCase`, `RealizarCompraUseCase`, `SolicitarRMAUseCase`, `CrearBuildUseCase`). No contienen reglas de negocio, solo orquestación. |
| `dto/` | **Objetos de transferencia**. `request/` para recibir datos del cliente, `response/` para devolver información. Separados de entidades de dominio. |
| `exception/` | **Excepciones de aplicación**. Ej: `RecursoNoEncontradoException`. |
| `mapper/` | **Mappers DTO-Dominio**. Transforman entre DTOs y objetos del dominio. |

### `infrastructure/`

Adaptadores que conectan el sistema con el mundo exterior.

| Paquete | Descripción |
| --- | --- |
| `rest/` | **Controladores REST**. Exponen la API HTTP. Validan con `@Valid`, mapean DTOs y delegan a los casos de uso. |
| `config/` | **Configuración Spring**. `BeanConfiguration` conecta puertos con adaptadores y crea beans de casos de uso/fábricas. `DomainServiceConfig` configura servicios de dominio. |
| `persistence/` | **Persistencia**. Implementación de los puertos del dominio. |
| `persistence/jpa/entity/` | **Entidades JPA**. Clases anotadas con `@Entity` que mapean a tablas. **No son las entidades de dominio** - solo para persistencia. Incluyen getters/setters y constructor vacío. |
| `persistence/jpa/embeddable/` | **Value Objects embebibles**. `@Embeddable` para mapear VOs como `Precio`, `EspecificacionTecnica`, `Garantia`. |
| `persistence/jpa/repository/` | **Spring Data JPA**. Interfaces que extienden `JpaRepository<*Entity, ID>`. Spring genera la implementación automáticamente. |
| `persistence/jpa/mapper/` | **Mappers Entity-Dominio**. Convierten entre `*Entity` (JPA) y agregados/VOs del dominio. Mantienen el dominio aislado de JPA. |
| `persistence/adapter/` | **Adaptadores de persistencia**. Implementan los puertos de `domain/repository/`. Marcados con `@Primary` para usar JPA sobre cualquier otra implementación. Traducen usando los mappers. |
| `security/` | **Seguridad**. Configuración JWT/OAuth2 Resource Server. |

## Cómo funciona

### Flujo de una petición

1. **REST Controller** recibe la petición HTTP, valida el DTO con Bean Validation
2. **Use Case (Application)** orquesta la operación, obteniendo agregados de los repositorios
3. **Domain** aplica las reglas de negocio e invariantes. Si hay violación, lanza `ReglaDominioException`
4. **Repository Port** (dominio) es llamado por el caso de uso
5. **JPA Adapter** (infraestructura) mapea dominio → entidad JPA, persiste con Spring Data, mapea entidad → dominio
6. **Respuesta** se retorna como DTO al cliente

### Persistencia con JPA

- **Separación dominio-persistencia**: Las entidades del dominio (`domain/entity/`) son puras (sin anotaciones JPA). Las entidades JPA viven en `infrastructure/persistence/jpa/entity/`.
- **Mapeo**: Los `*EntityMapper` convierten entre ambos mundos. El dominio nunca conoce a Hibernate/JPA.
- **Puertos y adaptadores**: Los casos de uso dependen de `domain/repository/*` (puertos). `*RepositoryJpaAdapter` con `@Primary` implementa estos puertos usando Spring Data.
- **Relaciones**: Se usan `@OneToMany` con `cascade = ALL, orphanRemoval = true` para agregados (ej: `Componente` → `UnidadInventario`, `Compra` → `ItemCompra`).
- **Value Objects**: Se mapean con `@Embedded`/`@Embeddable` (ej: `Precio`, `EspecificacionTecnica`).

### Base de datos

- **H2 en memoria**: `jdbc:h2:mem:compupartsdb`
- **Consola H2**: Disponible en `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:compupartsdb`
  - Usuario: `sa`
  - Contraseña: (vacía)
- **Esquema**: Generado automáticamente por Hibernate con `ddl-auto=create-drop`
- **Datos iniciales**: `src/main/resources/data.sql` se carga al iniciar

## Ejecutar el proyecto

```bash
# Compilar
./gradlew compileJava

# Ejecutar tests
./gradlew test

# Ejecutar aplicación
./gradlew bootRun
```

La aplicación estará disponible en `http://localhost:8080`. La documentación Swagger en `http://localhost:8080/swagger-ui.html`.


## Comparativo: Estado, congelamiento de precio e implementación del stock

| Observación que dieron | ¿Qué cambiamos? | Antes | Después | Dónde se ve (archivo) | Evidencia |
|---|---|---|---|---|---|
| **"Falta que el precio quede congelado al momento de la compra, no al momento de confirmar"** | Se congeló el precio desde el momento en que se reservan las unidades para el `Build`. El `ItemCompra` guarda el precio unitario exacto y `Compra` guarda el `precioCongelado` total. | El precio podía tomarse o recalcularse con el precio actual del `Componente` al confirmar la compra, lo que permitiría que un cambio de precio afectara una compra ya iniciada. | Al reservar, `Build.reservarUnidadesParaCompra()` toma `componente.getPrecio()` y crea `ItemCompra(unidadId, componenteId, numeroSerie, precioUnitario)`. `Compra.realizar()` recibe esos ítems, calcula el total con `calcularTotal()` y lo guarda como `precioCongelado`. Este valor nunca se vuelve a recalcular. | `domain/entity/Build.java` (líneas ~240-253) / `domain/entity/Compra.java` (líneas ~57-94) / `domain/valueobject/ItemCompra.java` | `ItemCompra` es inmutable (`record`) y valida sus atributos. `Compra` copia los ítems con `List.copyOf()` y congela el precio al crearla. |
| **"El precio debe guardarse también por unidad, no solo el total"** | Se modeló `ItemCompra` como parte del agregado `Compra` con su propio `precioUnitario`. Esto permite trazabilidad histórica por unidad vendida. | No quedaba registro claro del precio que se pagó por cada unidad física en particular. | Cada `ItemCompra` almacena `precioUnitario`, por lo que se puede auditar el precio exacto de cada `numeroSerie` comprado, incluso si el SKU cambia de precio después. | `domain/valueobject/ItemCompra.java` / `domain/entity/Compra.java` / `infrastructure/persistence/jpa/entity/ItemCompraEntity.java` / `infrastructure/persistence/jpa/embeddable/PrecioEmbeddable.java` | Persistido con `@OneToMany(cascade = ALL, orphanRemoval = true)` en `CompraEntity` y mapeado por `CompraEntityMapper`. |
| **"El stock estaba mal implementado. No es un contador, son unidades físicas"** | Se migró el concepto de "cantidad" a **unidades físicas** con `UnidadInventario`. Cada unidad tiene su propio `id`, `numeroSerie`, `estado` y reglas propias. | Se tendía a pensar en stock como un número entero (`cantidad`) sobre el `Componente`, lo cual no permitía controlar seriales, reservas por unidad o estados individuales. | `Componente` ahora gestiona una lista de `UnidadInventario`. Los estados se controlan por unidad con `EstadoUnidad` (`DISPONIBLE`, `RESERVADA`, `VENDIDA`, `PREVENTA`, `PREVENTA_RESERVADA`, `EN_RMA`, `DESCARTADA`). Las operaciones actúan sobre unidades específicas. | `domain/entity/Componente.java` (líneas ~99-234) / `domain/entity/UnidadInventario.java` / `domain/valueobject/EstadoUnidad.java` | `agregarUnidad()`, `reservarPrimeraUnidadDisponible()`, `venderUnidad()`, `liberarReservaCompra()`, `recibirPreventa()` modifican el estado de la unidad respetando sus invariantes. |

## Resumen

- **Precio congelado:** Implementado correctamente con `ItemCompra` + `precioCongelado` en `Compra`. Se congela al reservar unidades desde el `Build`, no al confirmar.
- **Stock:** Implementado con `UnidadInventario` y máquina de estados. Control por unidad física, con validaciones de unicidad de serial, flujos diferenciados para compra/preventa y liberación/reserva coherentes con las reglas de negocio.