# Mapeo de APIs y Ejemplos de Uso (Swagger)

La documentación interactiva está disponible en **Swagger UI**:

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## 1. Vendedores

Base: `/api/vendedores`

| Método | Endpoint | Descripción | Autorización |
| --- | --- | --- | --- |
| `POST` | `/api/vendedores` | Registrar un vendedor | Pública (en desarrollo) |
| `GET` | `/api/vendedores/{id}` | Consultar un vendedor por ID | Pública (en desarrollo) |
| `GET` | `/api/vendedores` | Listar todos los vendedores | Pública (en desarrollo) |

### Ejemplo: Registrar vendedor

**Request (POST /api/vendedores)**

```json
{
  "id": "v1",
  "nombre": "TecnoParts SAS",
  "tipo": "AUTORIZADO"
}
```

**Response (201 Created)**

```json
{
  "id": "v1",
  "nombre": "TecnoParts SAS",
  "tipo": "AUTORIZADO"
}
```

> `tipo` puede ser `AUTORIZADO` o `PARTICULAR`. Un vendedor **AUTORIZADO** puede vender con garantía y publicar en preventa. Un vendedor **PARTICULAR** vende de segunda mano (sin garantía) y no puede publicar en preventa.

## 2. Componentes

Base: `/api/componentes`

| Método | Endpoint | Descripción | Autorización |
| --- | --- | --- | --- |
| `POST` | `/api/componentes` | Registrar un componente (SKU) | Pública (en desarrollo) |
| `GET` | `/api/componentes` | Listar todos los componentes | Pública (en desarrollo) |
| `GET` | `/api/componentes/{id}` | Consultar un componente por ID | Pública (en desarrollo) |
| `POST` | `/api/componentes/{id}/unidades` | Agregar unidad física disponible | Pública (en desarrollo) |
| `POST` | `/api/componentes/{id}/preventa` | Publicar unidad en preventa | Pública (en desarrollo) |
| `POST` | `/api/componentes/{id}/agotar` | Marcar componente como agotado | Pública (en desarrollo) |
| `POST` | `/api/componentes/{id}/publicar` | Publicar nuevamente un componente agotado | Pública (en desarrollo) |
| `DELETE` | `/api/componentes/{id}` | Eliminar componente (borrado lógico) | Pública (en desarrollo) |

### Ejemplo: Registrar componente

**Request (POST /api/componentes)**

```json
{
  "id": "cpu-ryzen5",
  "nombre": "AMD Ryzen 5 5600",
  "categoria": "CPU",
  "socket": "AM4",
  "wattajeRequerido": 65,
  "tipoMemoria": null,
  "precioMonto": 850000,
  "precioMoneda": "COP",
  "vendedorId": "v1",
  "duracionGarantia": 36
}
```

**Response (201 Created)**

```json
{
  "id": "cpu-ryzen5",
  "nombre": "AMD Ryzen 5 5600",
  "categoria": "CPU",
  "socket": "AM4",
  "wattajeRequerido": 65,
  "tipoMemoria": null,
  "precio": {
    "monto": 850000,
    "moneda": "COP"
  },
  "vendedorId": "v1",
  "garantiaDias": 36,
  "estadoComercial": "PUBLICADO",
  "stockDisponible": 0,
  "stockPreventa": 0
}
```

> Categorías válidas: `CPU`, `MOTHERBOARD`, `RAM`, `GPU`, `PSU`, `STORAGE`, `CASE`, `COOLER`, `OTHER`. Si el vendedor es **PARTICULAR**, `duracionGarantia` debe ser `null`. Si es **AUTORIZADO**, debe tener garantía.

### Ejemplo: Agregar unidad

**Request (POST /api/componentes/cpu-ryzen5/unidades)**

```json
{
  "vendedorId": "v1",
  "numeroSerie": "SN-CPU-0001"
}
```

**Response (201 Created)**

```json
{
  "unidadId": "550e8400-e29b-41d4-a716-446655440001",
  "componenteId": "cpu-ryzen5",
  "numeroSerie": "SN-CPU-0001",
  "estado": "DISPONIBLE"
}
```

### Ejemplo: Publicar en preventa

**Request (POST /api/componentes/cpu-ryzen5/preventa)**

```json
{
  "vendedorId": "v1",
  "fechaEstimadaLlegada": "2026-12-01"
}
```

> Solo vendedores **AUTORIZADO** pueden publicar en preventa. La fecha estimada debe ser **posterior o igual** a la fecha actual.

## 3. Builds (Armados)

Base: `/api/builds`

| Método | Endpoint | Descripción | Autorización |
| --- | --- | --- | --- |
| `POST` | `/api/builds` | Crear build con componentes iniciales | Pública (en desarrollo) |
| `GET` | `/api/builds/{id}` | Consultar build por ID | Pública (en desarrollo) |
| `POST` | `/api/builds/{id}/componentes` | Agregar componente al build | Pública (en desarrollo) |
| `DELETE` | `/api/builds/{id}/componentes/{componenteId}` | Eliminar componente del build | Pública (en desarrollo) |
| `POST` | `/api/builds/{id}/validar` | Validar compatibilidad | Pública (en desarrollo) |
| `POST` | `/api/builds/{id}/listo-para-compra` | Marcar build como listo para compra | Pública (en desarrollo) |

### Ejemplo: Crear build con componentes

**Request (POST /api/builds)**

```json
{
  "componenteIds": ["cpu-ryzen5", "mb-b550", "ram-16gb", "psu-650"]
}
```

**Response (201 Created)**

```json
{
  "id": "b7f3e8a2-9c42-4b81-8d02-7ab6f9054321",
  "componentes": [
    {
      "id": "cpu-ryzen5",
      "nombre": "AMD Ryzen 5 5600",
      "categoria": "CPU",
      "precio": {
        "monto": 850000,
        "moneda": "COP"
      }
    },
    {
      "id": "mb-b550",
      "nombre": "ASUS B550M-A",
      "categoria": "MOTHERBOARD",
      "precio": {
        "monto": 650000,
        "moneda": "COP"
      }
    },
    {
      "id": "ram-16gb",
      "nombre": "Corsair Vengeance 16GB",
      "categoria": "RAM",
      "precio": {
        "monto": 320000,
        "moneda": "COP"
      }
    },
    {
      "id": "psu-650",
      "nombre": "EVGA 650W Bronze",
      "categoria": "PSU",
      "precio": {
        "monto": 420000,
        "moneda": "COP"
      }
    }
  ],
  "estado": "EN_CONSTRUCCION"
}
```

### Ejemplo: Validar compatibilidad

**Request (POST /api/builds/{id}/validar)**

Sin body requerido.

**Response (200 OK)**

```json
{
  "compatible": true,
  "motivos": []
}
```

Si hay incompatibilidad:

```json
{
  "compatible": false,
  "motivos": [
    "La PSU del Build tiene un wattaje insuficiente: 400W para un consumo de 130W",
    "El Build tiene incompatibilidades de socket sin resolver"
  ]
}
```

> **Reglas de compatibilidad:** Debe tener al menos CPU, Motherboard y PSU. La suma de consumo (componentes distintos a PSU) no debe exceder el wattaje de la PSU. El socket de CPU y Motherboard debe coincidir. Si hay RAM, su tipo de memoria debe coincidir con el de la Motherboard.

## 4. Compras

Base: `/api/compras`

| Método | Endpoint | Descripción | Autorización |
| --- | --- | --- | --- |
| `POST` | `/api/compras` | Realizar compra desde un Build | Pública (en desarrollo) |
| `GET` | `/api/compras/{id}` | Consultar compra por ID | Pública (en desarrollo) |
| `POST` | `/api/compras/{id}/confirmar` | Confirmar compra (vende las unidades reservadas) | Pública (en desarrollo) |
| `POST` | `/api/compras/{id}/cancelar` | Cancelar compra (libera las unidades reservadas) | Pública (en desarrollo) |
| `POST` | `/api/compras/{id}/reembolsar` | Reembolsar compra (requiere RMA aceptada) | Pública (en desarrollo) |

### Ejemplo: Realizar compra

**Request (POST /api/compras)**

```json
{
  "buildId": "b7f3e8a2-9c42-4b81-8d02-7ab6f9054321",
  "compradorId": "ana"
}
```

**Response (201 Created)**

```json
{
  "id": "C-100",
  "buildId": "b7f3e8a2-9c42-4b81-8d02-7ab6f9054321",
  "compradorId": "ana",
  "items": [
    {
      "unidadId": "550e8400-e29b-41d4-a716-446655440001",
      "componenteId": "cpu-ryzen5",
      "numeroSerie": "SN-CPU-0001",
      "precioUnitario": {
        "monto": 850000,
        "moneda": "COP"
      }
    },
    {
      "unidadId": "550e8400-e29b-41d4-a716-446655440003",
      "componenteId": "mb-b550",
      "numeroSerie": "SN-MB-0001",
      "precioUnitario": {
        "monto": 650000,
        "moneda": "COP"
      }
    },
    {
      "unidadId": "550e8400-e29b-41d4-a716-446655440004",
      "componenteId": "ram-16gb",
      "numeroSerie": "SN-RAM-0001",
      "precioUnitario": {
        "monto": 320000,
        "moneda": "COP"
      }
    },
    {
      "unidadId": "550e8400-e29b-41d4-a716-446655440005",
      "componenteId": "psu-650",
      "numeroSerie": "SN-PSU-0001",
      "precioUnitario": {
        "monto": 420000,
        "moneda": "COP"
      }
    }
  ],
  "precioCongelado": {
    "monto": 2240000,
    "moneda": "COP"
  },
  "fechaCompra": "2026-11-03T14:30:00",
  "estado": "PENDIENTE"
}
```

> Al realizar la compra, las unidades pasan a estado **RESERVADA** y los precios se **congelan** en `precioUnitario` y `precioCongelado`. El Build debe estar **LISTO_PARA_COMPRA** para poder comprarlo.

### Ejemplo: Confirmar compra

**Request (POST /api/compras/C-100/confirmar)**

Sin body requerido.

**Response (200 OK)**

```json
{
  "id": "C-100",
  "estado": "COMPLETADA",
  "precioCongelado": {
    "monto": 2240000,
    "moneda": "COP"
  }
}
```

> Al confirmar, las unidades pasan de **RESERVADA** a **VENDIDA** y el Build pasa a **COMPRADO**.

## 5. Preventas y Reservas

Base: `/api/preventas`

| Método | Endpoint | Descripción | Autorización |
| --- | --- | --- | --- |
| `POST` | `/api/preventas/reservar` | Reservar unidad en preventa | Pública (en desarrollo) |
| `POST` | `/api/preventas/cancelar` | Cancelar reserva de preventa | Pública (en desarrollo) |
| `POST` | `/api/preventas/recibir` | Recibir unidad en preventa (asigna número de serie) | Pública (en desarrollo) |
| `POST` | `/api/preventas/comprar` | Comprar reserva de preventa | Pública (en desarrollo) |
| `GET` | `/api/preventas/reservas/{id}` | Consultar reserva por ID | Pública (en desarrollo) |

### Ejemplo: Reservar preventa

**Request (POST /api/preventas/reservar)**

```json
{
  "componenteId": "cpu-ryzen5",
  "compradorId": "ana"
}
```

**Response (201 Created)**

```json
{
  "reservaId": "d8a42c61-7b9e-4f02-8e71-2ab543cf9021",
  "componenteId": "cpu-ryzen5",
  "unidadId": "550e8400-e29b-41d4-a716-446655440010",
  "compradorId": "ana",
  "fechaReserva": "2026-11-03T14:35:00",
  "estado": "ACTIVA"
}
```

> Solo se puede reservar una unidad que esté en estado **PREVENTA**. La unidad pasa a **PREVENTA_RESERVADA**.

### Ejemplo: Recibir preventa

**Request (POST /api/preventas/recibir)**

```json
{
  "componenteId": "cpu-ryzen5",
  "unidadId": "550e8400-e29b-41d4-a716-446655440010",
  "numeroSerie": "SN-CPU-PREV-001",
  "vendedorId": "v1"
}
```

**Response (200 OK)**

```json
{
  "unidadId": "550e8400-e29b-41d4-a716-446655440010",
  "componenteId": "cpu-ryzen5",
  "numeroSerie": "SN-CPU-PREV-001",
  "estado": "RESERVADA"
}
```

> Si la unidad estaba **PREVENTA_RESERVADA** (tenía comprador), al recibir pasa a **RESERVADA**. Si no tenía reserva, pasa a **DISPONIBLE**.

### Ejemplo: Comprar reserva de preventa

**Request (POST /api/preventas/comprar)**

```json
{
  "reservaId": "d8a42c61-7b9e-4f02-8e71-2ab543cf9021",
  "compradorId": "ana"
}
```

**Response (201 Created)**

```json
{
  "compraId": "C-101",
  "reservaId": "d8a42c61-7b9e-4f02-8e71-2ab543cf9021",
  "estado": "PENDIENTE"
}
```

## 6. RMA / Garantía

Base: `/api/rma`

| Método | Endpoint | Descripción | Autorización |
| --- | --- | --- | --- |
| `POST` | `/api/rma/solicitar` | Solicitar garantía/RMA | Pública (en desarrollo) |
| `POST` | `/api/rma/{id}/aceptar` | Aceptar solicitud RMA | Pública (en desarrollo) |
| `POST` | `/api/rma/{id}/rechazar` | Rechazar solicitud RMA | Pública (en desarrollo) |
| `GET` | `/api/rma/{id}` | Consultar solicitud por ID | Pública (en desarrollo) |
| `GET` | `/api/rma` | Listar todas las solicitudes RMA | Pública (en desarrollo) |

### Ejemplo: Solicitar RMA

**Request (POST /api/rma/solicitar)**

```json
{
  "compraId": "C-100",
  "numeroSerie": "SN-CPU-0001",
  "compradorId": "ana"
}
```

**Response (201 Created)**

```json
{
  "id": "RMA-001",
  "compraId": "C-100",
  "componenteId": "cpu-ryzen5",
  "numeroSerie": "SN-CPU-0001",
  "compradorId": "ana",
  "fechaCompra": "2026-11-03",
  "fechaSolicitud": "2026-11-04",
  "estado": "PENDIENTE"
}
```

> **Requisitos para solicitar RMA:** La unidad debe estar **VENDIDA**, pertenecer a un componente con garantía, el vendedor debe ser **AUTORIZADO**, la garantía debe estar vigente (la solicitud debe hacerse antes de que venza la garantía) y **no puede haber otra solicitud PENDIENTE** para la misma unidad.

### Ejemplo: Aceptar RMA

**Request (POST /api/rma/RMA-001/aceptar)**

Sin body requerido.

**Response (200 OK)**

```json
{
  "id": "RMA-001",
  "estado": "ACEPTADA"
}
```

> Al aceptar, la unidad pasa a estado **EN_RMA**.

### Ejemplo: Rechazar RMA

**Request (POST /api/rma/RMA-001/rechazar)**

```json
{
  "motivo": "El daño es por mal uso del usuario"
}
```

**Response (200 OK)**

```json
{
  "id": "RMA-001",
  "estado": "RECHAZADA",
  "motivoRechazo": "El daño es por mal uso del usuario"
}
```

> Para rechazar es **obligatorio** indicar un motivo.

### Ejemplo: Reembolsar compra con RMA aceptada

**Request (POST /api/compras/C-100/reembolsar)**

Sin body requerido.

**Response (200 OK)**

```json
{
  "id": "C-100",
  "estado": "REEMBOLSADA"
}
```

> Solo se puede reembolsar si todas las unidades de la compra tienen una solicitud RMA **ACEPTADA**. Al reembolsar, las unidades pasan a **DESCARTADA**.

## Consejos para probar en Swagger

1. Levanta la app con `./gradlew bootRun`
2. Abre [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
3. Usa los datos de `data.sql` o crea los tuyos siguiendo el orden: **Vendedor → Componente + Unidades → Build → Compra**
4. Para compras: primero crea el Build, agrégale componentes con stock, valida compatibilidad, márcalo como `LISTO_PARA_COMPRA`, luego realiza la compra
5. Revisa la consola H2 en [http://localhost:8080/h2-console](http://localhost:8080/h2-console) con `jdbc:h2:mem:compupartsdb` / `sa` / (vacío) para ver los cambios en tiempo real
