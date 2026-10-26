# Mapeo Dominio -> API

Base URL local: `http://localhost:8080`

Esta API usa el modelo B: un `Componente` es el SKU/anuncio y cada
`UnidadInventario` es una unidad física con su propio serial y estado.

## Convenciones

- Sin autenticación real: el vendedor se identifica con `vendedorId` y el
  comprador con `compradorId` en el cuerpo de la petición. En `DELETE` el actor
  se envía como query param (`vendedorId` o `compradorId`).
- Regla 9: solo el vendedor dueño del componente puede agregar unidades,
  publicar/recibir preventas, marcarlo agotado, volver a publicarlo, eliminarlo
  o resolver una RMA.
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
| `Compra.solicitarReembolso()` | `ReembolsarCompraUseCase` | `POST /api/compras/{id}/reembolsar` |

Estados de la compra: `PENDIENTE`, `COMPLETADA`, `CANCELADA`, `REEMBOLSADA`.
Una compra `PENDIENTE` reserva el stock; `CANCELADA` lo libera y `REEMBOLSADA`
descarta las unidades vendidas. Solo el comprador puede reembolsar una compra
`COMPLETADA` cuando todas sus unidades tienen una RMA `ACEPTADA`; el reembolso
es definitivo y las unidades pasan a `DESCARTADA`.

Ejemplo de reembolso:

```json
{
  "compradorId": "comprador-1",
  "motivo": "Falla de fábrica confirmada en RMA"
}
```

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

| Operación del dominio | Caso de uso | Método HTTP / Endpoint |
|---|---|---|
| `SolicitudRMA.crear()` | `SolicitarRMAUseCase` | `POST /api/rma` |
| Consultar solicitud | `ConsultarRMAUseCase` | `GET /api/rma/{id}` |
| Listar solicitudes | `ListarRMAUseCase` | `GET /api/rma` |
| `SolicitudRMA.aceptar()` | `AceptarRMAUseCase` | `POST /api/rma/{id}/aceptar` |
| `SolicitudRMA.rechazar()` | `RechazarRMAUseCase` | `POST /api/rma/{id}/rechazar` |

El comprador solicita el RMA sobre una unidad concreta de una compra
`COMPLETADA`, identificándola por su número de serie. Debe estar dentro de la
garantía y no puede existir otra solicitud `PENDIENTE` para esa unidad. Al
crearse, la unidad pasa a `EN_RMA`.

Solo el vendedor dueño del componente puede aceptar o rechazar la solicitud
(regla 9). Aceptar deja la unidad en `EN_RMA`; rechazar exige un motivo y la
devuelve a `VENDIDA`. Después de la aceptación, el comprador puede reembolsar la
compra con `POST /api/compras/{id}/reembolsar`; al hacerlo, todas las unidades
con RMA aceptada se descartan.

Ejemplo de solicitud:

```json
{
  "id": "rma-1",
  "compraId": "compra-1",
  "numeroSerie": "SER-123",
  "compradorId": "comprador-1",
  "fechaSolicitud": null
}
```

`fechaSolicitud` es opcional; si se omite se usa la fecha actual.

Para aceptar, el vendedor dueño envía:

```json
{"vendedorId": "vendedor-1"}
```

Para rechazar, además del vendedor, debe enviar el motivo:

```json
{"vendedorId": "vendedor-1", "motivo": "Daño no cubierto por la garantía"}
```
