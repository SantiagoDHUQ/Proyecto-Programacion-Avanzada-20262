# Manejo de Stock y Precio Congelado

## 1. Manejo de Stock

El stock se gestiona a nivel de **unidades físicas** (`UnidadInventario`), no directamente sobre el `Componente`. Cada `Componente` (SKU) puede tener múltiples unidades con su propio número de serie y estado.

### Estados de una unidad (`EstadoUnidad`)

| Estado | Descripción |
| --- | --- |
| `DISPONIBLE` | Unidad física lista para vender. |
| `RESERVADA` | Reservada para una compra pendiente o para una preventa ya recibida. |
| `VENDIDA` | Unidad ya vendida y confirmada. |
| `PREVENTA` | Unidad publicada en preventa, aún no ha llegado. Puede no tener número de serie. |
| `PREVENTA_RESERVADA` | Unidad en preventa que ya fue reservada por un comprador, pero aún no ha llegado. |
| `EN_RMA` | Unidad vendida que ingresó a trámite de garantía/RMA. |
| `DESCARTADA` | Unidad descartada definitivamente, no vuelve al stock. |

### Cómo se controla el stock

- **Agregar unidades**: `Componente.agregarUnidad()` añade una unidad `DISPONIBLE` con número de serie. Se valida que el serial sea único globalmente.
- **Publicar preventa**: `Componente.agregarPreventa()` crea una unidad `PREVENTA` con fecha estimada de llegada. Solo vendedores autorizados pueden hacerlo.
- **Reservar para compra**: `Build.reservarUnidadesParaCompra()` llama a `Componente.reservarPrimeraUnidadDisponible()` que toma la primera unidad `DISPONIBLE` y la pasa a `RESERVADA`. También existe `reservarUnidad()` para reservar una unidad específica.
- **Vender**: Al confirmar la compra, `Componente.venderUnidad()` pasa la unidad de `RESERVADA` a `VENDIDA`.
- **Liberar reserva**: Si se cancela una compra pendiente, `Componente.liberarReservaCompra()` devuelve la unidad a `DISPONIBLE` (si no tiene reserva de preventa activa).
- **Preventa**: Al reservar preventa, la unidad pasa a `PREVENTA_RESERVADA`. Al recibirla (`recibirPreventa()`), si estaba reservada pasa a `RESERVADA` (lista para comprar), si no estaba reservada pasa a `DISPONIBLE`.
- **Consulta de stock**: `Componente.stockDisponible()` cuenta unidades en estado `DISPONIBLE`. `Componente.estaDisponibleParaVenta()` retorna `true` si el componente está `PUBLICADO` y tiene stock disponible.

### Reglas importantes

- Una unidad solo puede reservarse si está `DISPONIBLE`.
- Una unidad solo puede venderse si está `RESERVADA`.
- Una unidad solo puede entrar a RMA si está `VENDIDA`.
- El número de serie es único globalmente en todo el catálogo.
- Un vendedor particular **no puede** publicar en preventa.
- Al eliminar un componente (`ELIMINADO`), no se puede agregar más stock ni venderlo.

## 2. Precio Congelado

Cuando se realiza una compra, el **precio se congela** en ese momento. Esto significa que, aunque el precio del `Componente` cambie después, la compra mantiene el precio que tenía al momento de reservarla.

### ¿Cómo funciona?

1. **Al reservar unidades para el Build**: `Build.reservarUnidadesParaCompra()` crea una lista de `ItemCompra` tomando `componente.getPrecio()` en ese instante. Cada `ItemCompra` guarda `unidadId`, `componenteId`, `numeroSerie` y `precioUnitario` (el precio del componente al momento de la reserva).

2. **Al crear la Compra**: `Compra.crear()` recibe esos `ItemCompra` ya generados con sus precios congelados. La compra almacena:
   - `items`: Lista de `ItemCompra` con el precio unitario congelado por cada unidad
   - `precioCongelado`: Precio total calculado al momento de crear la compra (`sumar()` de todos los precios unitarios)

3. **Persistencia**: Tanto `precioUnitario` en `ItemCompraEntity` como `precioCongelado` en `CompraEntity` se mapean con `PrecioEmbeddable` (`monto`, `moneda`), guardando los valores tal como estaban al momento de la compra.

### ¿Por qué es importante?

- **Consistencia**: El cliente paga lo que se le mostró/reservó al momento de iniciar la compra.
- **Integridad**: Si el vendedor sube o baja el precio del componente después, las compras ya realizadas no se ven afectadas.
- **Trazabilidad**: La compra guarda el precio histórico exacto de cada unidad vendida.

### Diferencia con el precio actual

| Concepto | Dónde vive | Cuándo se actualiza |
| --- | --- | --- |
| **Precio actual del Componente** | `Componente.precio` | Puede cambiar en el catálogo (a criterio del vendedor/sistema). |
| **Precio unitario congelado** | `ItemCompra.precioUnitario` | **Nunca** cambia. Se guarda al reservar. |
| **Precio congelado de la Compra** | `Compra.precioCongelado` | **Nunca** cambia. Es el total al momento de crear la compra. |
