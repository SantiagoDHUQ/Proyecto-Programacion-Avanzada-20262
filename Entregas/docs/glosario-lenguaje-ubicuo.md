# Glosario del Lenguaje Ubicuo - HardGamer Store (E-commerce de Hardware y Gaming)

## Conceptos Centrales

### Componente
**Definición:** Una pieza individual de hardware (placa de video, procesador, memoria RAM, fuente de poder, etc.) que se vende de forma independiente y que además puede formar parte de un Build. Cada Componente pertenece a una Categoría (GPU, CPU, Motherboard, RAM, PSU, Storage, Case) y tiene especificaciones técnicas propias (socket, wattaje, tipo de memoria, etc.).

**Sinónimos aceptados:** Pieza, Parte
**No usar:** "Producto" a secas cuando se hace referencia específicamente a una pieza de hardware dentro de un Build (sí puede usarse "Producto" para el catálogo general).

**Ejemplo de uso en código:**
```java
Componente gpu = new Componente(CategoriaComponente.GPU, "RTX 4070", socket: null, wattajeRequerido: 200);
```

---

### Build (Armado)
**Definición:** Un conjunto de Componentes seleccionados por el Comprador para ensamblar una PC completa (o parcial). El Build no es un producto físico en stock: es una combinación lógica de Componentes que debe pasar el Chequeo de Compatibilidad antes de poder agregarse al carrito como una unidad.

**Precondiciones:**
- Un Build debe tener al menos una Motherboard, un CPU y una PSU para considerarse "completo".
- Un Build no puede pasar a estado `LISTO_PARA_COMPRA` si tiene Componentes con incompatibilidades sin resolver.

**Ejemplo de uso:**
```java
Build miArmado = new Build();
miArmado.agregarComponente(cpu);
miArmado.agregarComponente(motherboard);
if (miArmado.esCompleto() && miArmado.esCompatible()) {
    miArmado.marcarComoListoParaCompra();
}
```

---

### Chequeo de Compatibilidad
**Definición:** Regla de negocio que valida que los Componentes dentro de un Build funcionen entre sí (mismo socket entre CPU y Motherboard, wattaje de la PSU suficiente para la suma de consumo de los demás Componentes, tipo de RAM soportado por la Motherboard, etc.). Es el corazón del dominio de armado de PCs y distingue este negocio de un e-commerce genérico.

**Precondiciones:**
- Solo se ejecuta cuando el Build tiene al menos 2 Componentes cargados.
- Si el chequeo falla, el Build queda en estado `INCOMPATIBLE` y no puede comprarse hasta corregirse.

**Ejemplo de uso en código:**
```java
ResultadoCompatibilidad resultado = servicioCompatibilidad.validar(miArmado);
if (!resultado.esCompatible()) {
    throw new IncompatibilidadException(resultado.getMotivo());
}
```

---

### RMA (Garantía por Falla de Hardware)
**Definición:** Proceso mediante el cual un Comprador solicita el reemplazo, reparación o reembolso de un Componente que presenta una falla de fábrica dentro del período de garantía del fabricante. Es distinto de una devolución genérica porque involucra un número de RMA, contacto con el fabricante/distribuidor, y un diagnóstico previo.

**Precondiciones:**
- El Componente debe estar dentro del período de garantía vigente.
- Debe existir un `numeroSerie` registrado en la Compra original para poder iniciar el RMA.
- No se puede iniciar un RMA sobre un Componente marcado como "usado sin garantía".

**Ejemplo de uso en código:**
```java
SolicitudRMA rma = new SolicitudRMA(componente, numeroSerie, motivoFalla);
if (componente.estaEnGarantia()) {
    servicioRMA.iniciar(rma);
}
```

---

### Preventa (Pre-order)
**Definición:** Modalidad de venta en la que un Comprador reserva un Componente que aún no está físicamente disponible en stock (por ejemplo, una GPU recién anunciada que llega en 30 días). El Comprador paga o reserva el cupo antes de que exista Stock real asignable.

**Precondiciones:**
- Solo aplica a Componentes marcados con `disponibilidad = PREVENTA`.
- Debe tener una `fechaEstimadaLlegada` definida.
- No se descuenta de Stock real hasta que el Componente ingresa físicamente al depósito.

**Ejemplo de uso:**
```java
if (componente.getDisponibilidad() == Disponibilidad.PREVENTA) {
    reservaService.crearReservaPreventa(comprador, componente, fechaEstimadaLlegada);
}
```

---

## Anti-patrones (Términos a EVITAR en nuestro proyecto)

| No usar | Usar |
|---|---|
| "Pieza suelta" / "Parte" (como clase de dominio) | Componente |
| "Combo de PC" / "PC armada" (como clase de dominio) | Build |
| "Devolución" (cuando es por falla de fábrica dentro de garantía) | RMA |
| "Reserva" a secas (cuando el ítem aún no tiene stock físico) | Preventa |
| "Validación" a secas (cuando se refiere a socket/wattaje/RAM) | Chequeo de Compatibilidad |
