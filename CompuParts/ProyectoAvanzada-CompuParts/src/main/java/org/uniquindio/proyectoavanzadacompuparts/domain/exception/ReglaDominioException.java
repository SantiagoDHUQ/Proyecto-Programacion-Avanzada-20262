package org.uniquindio.proyectoavanzadacompuparts.domain.exception;

/**
 * Excepción de dominio usada para indicar que se violó una regla del negocio.
 * Todas las invariantes del agregado y del modelo deben elevar esta excepción.
 */
public class ReglaDominioException extends RuntimeException {

    public ReglaDominioException(String mensaje) {
        super(mensaje);
    }
}
