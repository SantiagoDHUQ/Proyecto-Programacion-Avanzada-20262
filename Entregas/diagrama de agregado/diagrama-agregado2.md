Justificación del Límite del Agregado 

Dentro: La raíz SolicitudRMA y su EstadoSolicitudRMA (Value Object tipo enumeración) forman un único agregado porque comparten el mismo ciclo de vida transaccional. El estado no existe de manera independiente a la solicitud y cambia de forma controlada exclusivamente a través de los métodos de negocio de la raíz. 

Fuera: Componente, Usuario y Compra son entidades con su propio ciclo de vida independiente y repositorios propios. Mantenerlos fuera y referenciarlos únicamente por sus identificadores (componenteId, compradorId, compraId) previene bloqueos de concurrencia innecesarios y respeta los límites de consistencia transaccional exigidos por DDD. 

Invariantes del Agregado 

- Nunca puede crearse una SolicitudRMA si la fecha actual es posterior o igual a la fecha de compra más la duración de la garantía del componente (fechaCompra + duracionGarantiaMeses). 

- Siempre debe existir una referencia válida e inmutable a los IDs del componente (componenteId), del comprador (compradorId) y de la compra asociada (compraId) desde el momento de su instanciación. 

- Nunca puede modificarse el estado de una SolicitudRMA mediante métodos genéricos tipo setter; los cambios de estado deben obedecer exclusivamente a reglas de negocio explícitas (como procesar() o rechazar()). 

- Siempre debe inicializarse una nueva solicitud con el estado PENDIENTE por defecto antes de cualquier evaluación de soporte técnico. 

