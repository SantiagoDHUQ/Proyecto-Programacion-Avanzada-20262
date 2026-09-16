package org.uniquindio.proyectoavanzadacompuparts.aplication.entity;



import org.uniquindio.proyectoavanzadacompuparts.aplication.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.aplication.valueObject.Disponibilidad;
import org.uniquindio.proyectoavanzadacompuparts.aplication.valueObject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.aplication.valueObject.Precio;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa una pieza de hardware (GPU, CPU, RAM, PSU, etc.)
 * que se vende de forma independiente o como parte de un Build.
 */
public class Componente {

    private final UUID id;
    private final String nombre;
    private final String categoria;
    private EspecificacionTecnica especificacion;
    private Precio precio;
    private Disponibilidad disponibilidad;
    private LocalDate fechaEstimadaLlegada;

    public Componente(String nombre, String categoria, EspecificacionTecnica especificacion,
                      Precio precio, Disponibilidad disponibilidad) {
        this.id = UUID.randomUUID();
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        this.categoria = Objects.requireNonNull(categoria, "La categoría no puede ser nula");
        this.especificacion = Objects.requireNonNull(especificacion, "La especificación no puede ser nula");
        this.precio = Objects.requireNonNull(precio, "El precio no puede ser nulo");
        this.disponibilidad = Objects.requireNonNull(disponibilidad, "La disponibilidad no puede ser nula");
    }

    /**
     * Regla de dominio: no se puede poner un componente en preventa
     * si no se conoce su fecha estimada de llegada.
     */
    public void marcarComoPreventa(LocalDate fechaEstimadaLlegada) {
        if (fechaEstimadaLlegada == null) {
            throw new ReglaDominioException(
                    "No se puede poner en preventa un componente sin fecha estimada de llegada");
        }
        this.disponibilidad = Disponibilidad.PREVENTA;
        this.fechaEstimadaLlegada = fechaEstimadaLlegada;
    }

    public void actualizarPrecio(Precio nuevoPrecio) {
        this.precio = Objects.requireNonNull(nuevoPrecio, "El nuevo precio no puede ser nulo");
    }

    public void marcarComoAgotado() {
        this.disponibilidad = Disponibilidad.AGOTADO;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public EspecificacionTecnica getEspecificacion() {
        return especificacion;
    }

    public Precio getPrecio() {
        return precio;
    }

    public Disponibilidad getDisponibilidad() {
        return disponibilidad;
    }

    public LocalDate getFechaEstimadaLlegada() {
        return fechaEstimadaLlegada;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Componente)) return false;
        Componente that = (Componente) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}