package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import lombok.Getter;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.util.Objects;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Entidad que representa al vendedor del Componente.
 * El tipo determina si el vendedor puede publicar en Preventa y manejar RMA.
 */
@Getter
public class Vendedor {

    public enum TipoVendedor {
        AUTORIZADO,
        PARTICULAR
    }

    private final String id;
    private final String nombre;
    private final TipoVendedor tipo;

    private Vendedor(String id, String nombre, TipoVendedor tipo) {
        exigir(id != null && !id.isBlank(), "El identificador del vendedor no puede ser nulo");
        exigir(nombre != null && !nombre.isBlank(), "El nombre del vendedor no puede ser nulo");
        exigir(tipo != null, "El tipo del vendedor no puede ser nulo");
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public static Vendedor crear(String id, String nombre, String tipo) {
        return new Vendedor(id, nombre, convertirTipo(tipo));
    }

    //valueOf lanza una excepción diferente, se atrapa para que se comporte como una ReglaDominioException
    private static TipoVendedor convertirTipo(String tipo) {
        exigir(tipo != null && !tipo.isBlank(), "El tipo del vendedor es obligatorio");
        try {
            return TipoVendedor.valueOf(tipo.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ReglaDominioException("Tipo de vendedor inválido: " + tipo + " (use AUTORIZADO o PARTICULAR)");
        }
    }

    public boolean esAutorizado() {
        return this.tipo == TipoVendedor.AUTORIZADO;
    }

    public boolean esParticular() {
        return this.tipo == TipoVendedor.PARTICULAR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vendedor vendedor)) return false;
        return id.equals(vendedor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
