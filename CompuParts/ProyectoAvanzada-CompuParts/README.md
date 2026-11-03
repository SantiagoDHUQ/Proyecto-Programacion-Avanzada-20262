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
