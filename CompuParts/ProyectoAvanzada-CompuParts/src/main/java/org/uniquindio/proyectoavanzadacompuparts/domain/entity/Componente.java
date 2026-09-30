package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Disponibilidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Garantia;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Componente: el ciclo de vida del componente se guarda dentro de esta raíz. 
 * SolicitudRMA NO vive dentro del agregado, se referencia por id desde fuera.
 */
public class Componente {

    private final UUID id;
    private final String nombre;
    private final CategoriaComponente categoria;
    private final EspecificacionTecnica especificacion;
    private final Precio precio;
    private final Vendedor vendedor;
    private final String numeroSerie;
    private final LocalDate fechaCompra;
    private final Garantia garantia;
    private final boolean nuevo;
    private Disponibilidad disponibilidad;
    private LocalDate fechaEstimadaLlegada;
    private boolean usadoSinGarantia;

    private Componente(String nombre, CategoriaComponente categoria, EspecificacionTecnica especificacion,
                       Precio precio, Disponibilidad disponibilidad, Vendedor vendedor, String numeroSerie,
                       LocalDate fechaCompra, boolean nuevo) {
        this.id = UUID.randomUUID();
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        this.categoria = Objects.requireNonNull(categoria, "La categoría no puede ser nula");
        this.especificacion = Objects.requireNonNull(especificacion, "La especificación no puede ser nula");
        this.precio = Objects.requireNonNull(precio, "El precio no puede ser nulo");
        this.disponibilidad = Objects.requireNonNull(disponibilidad, "La disponibilidad no puede ser nula");
        this.vendedor = Objects.requireNonNull(vendedor, "El vendedor no puede ser nulo");
        this.numeroSerie = Objects.requireNonNull(numeroSerie, "El número de serie no puede ser nulo");
        this.fechaCompra = fechaCompra != null ? fechaCompra : LocalDate.now();
        this.garantia = new Garantia(this.fechaCompra, 365);
        this.nuevo = nuevo;
        if (nombre.isBlank()) {
            throw new ReglaDominioException("El nombre del componente no puede estar vacío");
        }
        if (numeroSerie.isBlank()) {
            throw new ReglaDominioException("El número de serie no puede estar vacío");
        }
    }

    public static Componente crear(String nombre, CategoriaComponente categoria, EspecificacionTecnica especificacion,
                                  Precio precio, Disponibilidad disponibilidad, Vendedor vendedor, String numeroSerie) {
        return new Componente(nombre, categoria, especificacion, precio, disponibilidad, vendedor, numeroSerie, LocalDate.now(), true);
    }

    public static Componente crear(String nombre, CategoriaComponente categoria, EspecificacionTecnica especificacion,
                                  Precio precio, Disponibilidad disponibilidad, Vendedor vendedor, String numeroSerie,
                                  LocalDate fechaCompra, boolean nuevo) {
        return new Componente(nombre, categoria, especificacion, precio, disponibilidad, vendedor, numeroSerie, fechaCompra, nuevo);
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

    public void marcarComoUsadoSinGarantia() {
        this.usadoSinGarantia = true;
    }

    public void crearSolicitudRMA(LocalDate fechaCompraSolicitud, int duracionGarantia, LocalDate fechaActual) {
        if (usadoSinGarantia) {
            throw new ReglaDominioException("Un componente usado sin garantía nunca puede tener una SolicitudRMA");
        }
        if (!new Garantia(fechaCompraSolicitud, duracionGarantia).estaVigente(fechaActual)) {
            throw new ReglaDominioException("La garantía ya expiró; no se puede crear una SolicitudRMA");
        }
    }

    public boolean esActivoEnBuild() {
        return disponibilidad != Disponibilidad.AGOTADO;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public CategoriaComponente getCategoria() {
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

    public Vendedor getVendedor() {
        return vendedor;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    public Garantia getGarantia() {
        return garantia;
    }

    public boolean isNuevo() {
        return nuevo;
    }

    public boolean isUsadoSinGarantia() {
        return usadoSinGarantia;
    }

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
