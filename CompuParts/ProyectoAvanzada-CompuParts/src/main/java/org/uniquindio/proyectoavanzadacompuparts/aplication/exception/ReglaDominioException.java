package org.uniquindio.proyectoavanzadacompuparts.aplication.exception;

/**
 * Excepción lanzada cuando se viola una regla de negocio del dominio
 * (ej: incompatibilidad de componentes, preventa sin fecha, RMA fuera de garantía).
 */
public class ReglaDominioException extends RuntimeException {

    public ReglaDominioException(String mensaje) {
        super(mensaje);
    }

    public ReglaDominioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}