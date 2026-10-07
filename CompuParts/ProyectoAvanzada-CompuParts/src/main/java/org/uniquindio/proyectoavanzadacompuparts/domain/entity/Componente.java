package org.uniquindio.proyectoavanzadacompuparts.domain.entity;
import lombok.Getter;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.*;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Agregado Componente.
 * 
 * Invariantes que garantiza:
 *  - Un componente de un vendedor particular nunca tiene garantía.
 *  - Un vendedor autorizado debe registrar la garantía de sus componentes.
 *  - Un componente en PREVENTA solo puede ser publicado por un vendedor autorizado.
 *  - Un componente en PREVENTA requiere obligatoriamente una fecha estimada de llegada.
 */
@Getter
public class Componente {

    private final String id;
    private final String nombre;
    private final CategoriaComponente categoria;
    private final EspecificacionTecnica especificacion;
    private final Precio precio;
    private final Vendedor vendedor;
    private final String numeroSerie;
    private final Garantia garantia;          // null cuando es de segunda mano
    private Disponibilidad disponibilidad;
    private LocalDate fechaEstimadaLlegada;

    private Componente(String id, String nombre, CategoriaComponente categoria, EspecificacionTecnica especificacion,
                       Precio precio, Vendedor vendedor, String numeroSerie, Garantia garantia) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.especificacion = especificacion;
        this.precio = precio;
        this.vendedor = vendedor;
        this.numeroSerie = numeroSerie;
        this.garantia = garantia;
        this.disponibilidad = Disponibilidad.DISPONIBLE;   // la preventa entra por pasarAPreventa()
    }

    public static Componente crear(String id, String nombre, CategoriaComponente categoria,
                                   EspecificacionTecnica especificacion, Precio precio, Vendedor vendedor,
                                   String numeroSerie, Integer duracionGarantia) {
        exigir(id != null && !id.isBlank(), "El identificador del componente es obligatorio");
        exigir(nombre != null && !nombre.isBlank(), "El nombre del componente no puede estar vacío");
        exigir(categoria != null, "La categoría es obligatoria");
        exigir(especificacion != null, "La especificación es obligatoria");
        exigir(precio != null, "El precio es obligatorio");
        exigir(vendedor != null, "El vendedor es obligatorio");
        exigir(numeroSerie != null && !numeroSerie.isBlank(), "El número de serie no puede estar vacío");

        boolean conGarantia = duracionGarantia != null;
        exigir(!(vendedor.esAutorizado() && !conGarantia), "Un vendedor autorizado debe registrar la garantía");
        exigir(!(vendedor.esParticular() && conGarantia), "Un componente de segunda mano no tiene garantía");

        Garantia garantia = conGarantia ? new Garantia(duracionGarantia) : null;
        return new Componente(id, nombre, categoria, especificacion, precio, vendedor, numeroSerie, garantia);
    }

    /**
     * Invariante del agregado: un Componente solo puede pasar a PREVENTA si tiene fecha estimada.
     */
    public void pasarAPreventa(LocalDate fechaEstimadaLlegada) {
        if (fechaEstimadaLlegada == null) {
            throw new ReglaDominioException("Un componente en preventa debe tener fecha estimada de llegada");
        }
        if (vendedor.esParticular()) {
            throw new ReglaDominioException("Un vendedor particular no puede publicar componentes en preventa");
        }
        if (!vendedor.esAutorizado()) {
            throw new ReglaDominioException("Solo un vendedor autorizado puede publicar en preventa");
        }
        this.disponibilidad = Disponibilidad.PREVENTA;
        this.fechaEstimadaLlegada = fechaEstimadaLlegada;
    }

    public void marcarAgotado() {
        this.disponibilidad = Disponibilidad.AGOTADO;
    }

    // TODO: se reemplaza por Build.estaActivo() cuando se reescriba EliminarComponenteUseCase
    public boolean esActivoEnBuild(Build build) {
        return !(build.getEstado().equals(EstadoBuild.CANCELADO) || build.getEstado().equals(EstadoBuild.COMPRADO));
    }

    /** Reglas 3, 5 y 8: con garantía vigente (hoy < fechaCompra + días) y vendedor autorizado. */
    public boolean admiteRMA(LocalDate fechaCompra, LocalDate hoy) {
        return garantia != null && vendedor.esAutorizado() && garantia.estaVigente(fechaCompra, hoy);
    }

    public Optional<Garantia> getGarantia() { return Optional.ofNullable(garantia); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Componente componente)) return false;
        return id.equals(componente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}