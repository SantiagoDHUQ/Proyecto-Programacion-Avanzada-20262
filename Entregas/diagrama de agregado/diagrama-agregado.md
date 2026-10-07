1. *Un pedido nunca puede ser confirmado o pagado* si la lista de ItemPedido está vacía (debe tener al menos un componente).
2. *El total del pedido siempre debe ser* exactamente igual a la suma de la cantidad multiplicada por el precio unitario de todos sus ítems internos.
3. *Un pedido nunca puede ser modificado* (no se pueden agregar ni remover ítems) una vez que su estado haya pasado a PAGADO, EN_PREPARACION o ENVIADO.
4. *La cantidad de un ítem siempre debe ser* mayor a cero al momento de agregarlo al pedido.