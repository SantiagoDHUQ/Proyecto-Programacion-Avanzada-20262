package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa al vendedor del Componente.
 * El tipo determina si el vendedor puede publicar en Preventa o manejar RMA.
 */
public class Vendedor {

    public enum TipoVendedor {
        AUTORIZADO,
        PARTICULAR
    }

    private final UUID id;
    private final String nombre;
    private final TipoVendedor tipo;

    private Vendedor(String nombre, TipoVendedor tipo) {
        this.id = UUID.randomUUID();
        this.nombre = Objects.requireNonNull(nombre, "El nombre del vendedor no puede ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "El tipo del vendedor no puede ser nulo");
        if (nombre.isBlank()) {
            throw new ReglaDominioException("El nombre del vendedor no puede estar vacío");
        }
    }

    public static Vendedor crear(String nombre, String tipo) {
        return new Vendedor(nombre, TipoVendedor.valueOf(tipo.toUpperCase()));
    }

    public boolean esAutorizado() {
        return this.tipo == TipoVendedor.AUTORIZADO;
    }

    public boolean esParticular() {
        return this.tipo == TipoVendedor.PARTICULAR;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoVendedor getTipo() {
        return tipo;
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
