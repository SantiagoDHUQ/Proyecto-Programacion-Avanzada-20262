# Mapeo Dominio -> API

Base URL local: `http://localhost:8080`

Esta API usa el modelo B: un `Componente` es el SKU/anuncio y cada
`UnidadInventario` es una unidad física con su propio serial y estado.

## Convenciones

- Sin autenticación real: el vendedor se identifica con `vendedorId` y el
  comprador con `compradorId` en el cuerpo de la petición. En `DELETE` el actor
  se envía como query param (`vendedorId` o `compradorId`).
- Regla 9: solo el vendedor dueño del componente puede agregar unidades,
  publicar/recibir preventas, marcarlo agotado, volver a publicarlo o eliminarlo.
- "Eliminar" es un borrado lógico: el componente pasa a `ELIMINADO` y conserva
  sus unidades e historial.
- Los estados finales de una compra (`CANCELADA`, `REEMBOLSADA`) no se pueden
  modificar. Una compra `COMPLETADA` solo puede pasar a `REEMBOLSADA`.
- Una preventa no se puede agregar a un Build ni comprar antes de que la unidad
  llegue. El comprador puede reservarla y, cuando llega, comprarla directamente.

## Vendedor

| Operación del dominio | Caso de uso | Método HTTP / Endpoint |
|---|---|---|
| `Vendedor.crear()` | `RegistrarVendedorUseCase` | `POST /api/vendedores` |
| Consultar vendedor | `ConsultarVendedorUseCase` | `GET /api/vendedores/{id}` |
| Listar vendedores | `ListarVendedoresUseCase` | `GET /api/vendedores` |

## Componente (SKU)

| Operación del dominio | Caso de uso | Método HTTP / Endpoint |
|---|---|---|
| `Componente.crear()` | `RegistrarComponenteUseCase` | `POST /api/componentes` |
| Consultar componente y sus unidades | `ConsultarComponenteUseCase` | `GET /api/componentes/{id}` |
| Listar catálogo (excluye eliminados) | `ListarComponentesUseCase` | `GET /api/componentes` |
| `Componente.agregarUnidad()` | `AgregarUnidadUseCase` | `POST /api/componentes/{id}/unidades` |
| `Componente.agregarPreventa()` | `PublicarPreventaUseCase` | `POST /api/componentes/{id}/preventas` |
| `Componente.recibirPreventa()` | `RecibirPreventaUseCase` | `POST /api/componentes/{id}/unidades/{unidadId}/recibir` |
| `Componente.marcarAgotado()` | `MarcarComponenteAgotadoUseCase` | `PUT /api/componentes/{id}/agotado` |
| `Componente.publicar()` | `PublicarComponenteUseCase` | `PUT /api/componentes/{id}/publicar` |
| `Componente.eliminar()` | `EliminarComponenteUseCase` | `DELETE /api/componentes/{id}?vendedorId=...` |

Estados comerciales del SKU: `PUBLICADO`, `AGOTADO`, `ELIMINADO`.

## UnidadInventario

| Operación del dominio | Caso de uso | Método HTTP / Endpoint |
|---|---|---|
| `UnidadInventario.crearDisponible()` | `AgregarUnidadUseCase` | `POST /api/componentes/{id}/unidades` |
| Consultar unidad | `ConsultarUnidadUseCase` | `GET /api/componentes/{id}/unidades/{unidadId}` |
| `UnidadInventario.crearPreventa()` | `PublicarPreventaUseCase` | `POST /api/componentes/{id}/preventas` |
| `UnidadInventario.recibirPreventa()` | `RecibirPreventaUseCase` | `POST /api/componentes/{id}/unidades/{unidadId}/recibir` |

Estados de una unidad: `DISPONIBLE`, `RESERVADA`, `VENDIDA`, `PREVENTA`,
`PREVENTA_RESERVADA`, `EN_RMA`, `DESCARTADA`.

## ReservaPreventa

| Operación del dominio | Caso de uso | Método HTTP / Endpoint |
|---|---|---|
| `ReservaPreventa.crear()` | `ReservarPreventaUseCase` | `POST /api/componentes/{componenteId}/unidades/{unidadId}/reservas-preventa` |
| Consultar reserva | `ConsultarReservaPreventaUseCase` | `GET /api/reservas-preventa/{id}` |
| `ReservaPreventa.cancelar()` | `CancelarReservaPreventaUseCase` | `DELETE /api/reservas-preventa/{id}?compradorId=...` |
| `ReservaPreventa.iniciarCompra()` + `Compra.realizar()` | `ComprarReservaPreventaUseCase` | `POST /api/reservas-preventa/{id}/compra` |
| `ReservaPreventa.convertir()` | `ConfirmarCompraUseCase` | `POST /api/compras/{id}/confirmar` |
| `ReservaPreventa.volverAActiva()` | `CancelarCompraUseCase` | `POST /api/compras/{id}/cancelar` |

Estados de la reserva: `ACTIVA`, `EN_COMPRA`, `CONVERTIDA`, `CANCELADA`.

Flujo de compra directa de una preventa:

1. El vendedor publica la unidad en preventa.
2. El comprador la reserva: `ACTIVA`, unidad `PREVENTA_RESERVADA`.
3. El vendedor recibe la unidad y le asigna serial: unidad `RESERVADA`.
4. El comprador compra la reserva: reserva `EN_COMPRA`, compra `PENDIENTE`.
5. Al confirmar la compra: reserva `CONVERTIDA`, unidad `VENDIDA`.
6. Si la compra se cancela antes de confirmar: reserva vuelve a `ACTIVA` y la
   unidad sigue `RESERVADA` para el comprador.

## Compra

| Operación del dominio | Caso de uso | Método HTTP / Endpoint |
|---|---|---|
| `Compra.realizar()` sobre un Build | `RealizarCompraUseCase` | `POST /api/compras` |
| Consultar compra | `ConsultarCompraUseCase` | `GET /api/compras/{id}` |
| `Compra.confirmar()` | `ConfirmarCompraUseCase` | `POST /api/compras/{id}/confirmar` |
| `Compra.cancelar()` | `CancelarCompraUseCase` | `POST /api/compras/{id}/cancelar` |

Estados de la compra: `PENDIENTE`, `COMPLETADA`, `CANCELADA`, `REEMBOLSADA`.
Una compra `PENDIENTE` reserva el stock; `CANCELADA` lo libera y `REEMBOLSADA`
descarta las unidades vendidas. El reembolso se gestionará junto con el RMA.

## Build

| Operación del dominio | Caso de uso | Método HTTP / Endpoint |
|---|---|---|
| `Build.crear()` | `CrearBuildUseCase` | `POST /api/builds` |
| Consultar Build | `ConsultarBuildUseCase` | `GET /api/builds/{buildId}` |
| `Build.agregarComponente()` | `AgregarComponenteABuildUseCase` | `POST /api/builds/{buildId}/componentes` |
| `Build.validarCompatibilidad()` | `ValidarCompatibilidadBuildUseCase` | `POST /api/builds/{buildId}/validacion` |
| `Build.marcarComoListoParaCompra()` | `MarcarBuildListoParaCompraUseCase` | `POST /api/builds/{buildId}/listo-para-compra` |
| `Compra.realizar()` con `buildId` | `RealizarCompraUseCase` | `POST /api/compras` |

Un Build no puede contener dos unidades del mismo SKU. La compra de un Build
marca el Build como `COMPRADO`; un Build comprado o cancelado ya no se modifica.

## RMA

Pendiente para el siguiente bloque. La regla 9 exige que solo el vendedor dueño
del componente opere y resuelva el RMA; estos endpoints aún no se exponen en la
API.
