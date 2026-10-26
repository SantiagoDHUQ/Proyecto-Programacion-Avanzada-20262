package org.uniquindio.proyectoavanzadacompuparts.application.exception;

/**
 * Se lanza cuando un caso de uso no encuentra el recurso que necesita.
 * No es una violación de regla de negocio: es un 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
