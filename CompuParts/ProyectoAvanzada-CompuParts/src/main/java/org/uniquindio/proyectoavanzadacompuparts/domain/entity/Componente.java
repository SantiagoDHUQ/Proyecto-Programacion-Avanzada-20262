package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoComercial;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Garantia;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Agregado Componente (SKU).
 *
 * <p>El Componente representa el anuncio/modelo que se vende. El stock físico vive
 * en sus {@link UnidadInventario}, cada una con su propio número de serie y estado.</p>
 *
 * <p>Invariantes que garantiza:
 * <ul>
 *   <li>Un componente de un vendedor particular nunca tiene garantía.</li>
 *   <li>Un vendedor autorizado debe registrar la garantía de sus componentes.</li>
 *   <li>Solo un vendedor autorizado puede publicar unidades en preventa.</li>
 *   <li>Una preventa exige una fecha estimada de llegada que no sea anterior a hoy.</li>
 *   <li>Una unidad no puede estar dos veces en el mismo Componente por número de serie.</li>
 *   <li>Una unidad solo se reserva si está DISPONIBLE y solo se vende si está RESERVADA.</li>
 * </ul>
 */
public class Componente {

    private final String id;
    private final String nombre;
    private final CategoriaComponente categoria;
    private final EspecificacionTecnica especificacion;
    private final Precio precio;
    private final Vendedor vendedor;
    private final Garantia garantia;          // null cuando es de segunda mano
    private final List<UnidadInventario> unidades;
    private EstadoComercial estadoComercial;

    private Componente(String id, String nombre, CategoriaComponente categoria,
                       EspecificacionTecnica especificacion, Precio precio, Vendedor vendedor,
                       Garantia garantia, List<UnidadInventario> unidades,
                       EstadoComercial estadoComercial) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.especificacion = especificacion;
        this.precio = precio;
        this.vendedor = vendedor;
        this.garantia = garantia;
        this.unidades = unidades == null ? new ArrayList<>() : new ArrayList<>(unidades);
        this.estadoComercial = estadoComercial == null ? EstadoComercial.PUBLICADO : estadoComercial;
    }

    public static Componente crear(String id, String nombre, CategoriaComponente categoria,
                                   EspecificacionTecnica especificacion, Precio precio, Vendedor vendedor,
                                   Integer duracionGarantia) {
        exigir(id != null && !id.isBlank(), "El identificador del componente es obligatorio");
        exigir(nombre != null && !nombre.isBlank(), "El nombre del componente no puede estar vacío");
        exigir(categoria != null, "La categoría es obligatoria");
        exigir(especificacion != null, "La especificación es obligatoria");
        exigir(precio != null, "El precio es obligatorio");
        exigir(vendedor != null, "El vendedor es obligatorio");

        boolean conGarantia = duracionGarantia != null;
        exigir(!(vendedor.esAutorizado() && !conGarantia), "Un vendedor autorizado debe registrar la garantía");
        exigir(!(vendedor.esParticular() && conGarantia), "Un componente de segunda mano no tiene garantía");

        Garantia garantia = conGarantia ? new Garantia(duracionGarantia) : null;
        return new Componente(id, nombre, categoria, especificacion, precio, vendedor, garantia,
                new ArrayList<>(), EstadoComercial.PUBLICADO);
    }

    public static Componente reconstruir(String id, String nombre, CategoriaComponente categoria,
                                         EspecificacionTecnica especificacion, Precio precio, Vendedor vendedor,
                                         Garantia garantia, List<UnidadInventario> unidades,
                                         EstadoComercial estadoComercial) {
        exigir(id != null && !id.isBlank(), "El identificador del componente es obligatorio");
        exigir(nombre != null && !nombre.isBlank(), "El nombre del componente no puede estar vacío");
        exigir(categoria != null, "La categoría es obligatoria");
        exigir(especificacion != null, "La especificación es obligatoria");
        exigir(precio != null, "El precio es obligatorio");
        exigir(vendedor != null, "El vendedor es obligatorio");
        exigir(estadoComercial != null, "El estado comercial es obligatorio");
        return new Componente(id, nombre, categoria, especificacion, precio, vendedor, garantia,
                unidades, estadoComercial);
    }

    // ---------- Unidades de inventario ----------

    /**
     * Agrega una unidad físicamente disponible.
     */
    public UnidadInventario agregarUnidad(String numeroSerie) {
        exigir(estadoComercial != EstadoComercial.ELIMINADO,
                "No se puede agregar stock a un componente eliminado");
        exigir(numeroSerie != null && !numeroSerie.isBlank(), "El número de serie es obligatorio");
        exigir(!existeNumeroSerie(numeroSerie),
                "Ya existe una unidad con ese número de serie en el componente");

        UnidadInventario unidad = UnidadInventario.crearDisponible(numeroSerie);
        unidades.add(unidad);
        return unidad;
    }

    /**
     * Publica una unidad en preventa. El comprador podrá reservarla, pero no
     * agregarla a un Build ni comprarla hasta que llegue.
     */
    public UnidadInventario agregarPreventa(LocalDate fechaEstimadaLlegada) {
        exigir(estadoComercial != EstadoComercial.ELIMINADO,
                "No se puede agregar preventa a un componente eliminado");
        exigir(vendedor.esAutorizado(), "Solo un vendedor autorizado puede publicar en preventa");
        exigir(!vendedor.esParticular(), "Un vendedor particular no puede publicar componentes en preventa");

        UnidadInventario unidad = UnidadInventario.crearPreventa(fechaEstimadaLlegada);
        unidades.add(unidad);
        return unidad;
    }

    public UnidadInventario unidadPorId(UUID unidadId) {
        exigir(unidadId != null, "La unidad es obligatoria");
        return unidades.stream()
                .filter(unidad -> unidad.getId().equals(unidadId))
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException("No existe esa unidad en el componente"));
    }

    public UnidadInventario unidadPorNumeroSerie(String numeroSerie) {
        exigir(numeroSerie != null && !numeroSerie.isBlank(), "El número de serie es obligatorio");
        return unidades.stream()
                .filter(unidad -> unidad.getNumeroSerie() != null
                        && unidad.getNumeroSerie().equalsIgnoreCase(numeroSerie.trim()))
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException(
                        "No existe una unidad con ese número de serie en el componente"));
    }

    public UnidadInventario reservarUnidad(UUID unidadId) {
        UnidadInventario unidad = unidadPorId(unidadId);
        unidad.reservarParaCompra();
        return unidad;
    }

    /**
     * Reserva la primera unidad disponible. Se usa cuando la compra solo conoce
     * el SKU y debe escoger una unidad física.
     */
    public UnidadInventario reservarPrimeraUnidadDisponible() {
        UnidadInventario unidad = unidades.stream()
                .filter(UnidadInventario::estaDisponible)
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException("El componente no tiene stock disponible"));
        unidad.reservarParaCompra();
        return unidad;
    }

    public void venderUnidad(UUID unidadId) {
        unidadPorId(unidadId).vender();
    }

    public void liberarReservaCompra(UUID unidadId) {
        unidadPorId(unidadId).liberarReservaCompra();
    }

    public void reservarPreventa(UUID unidadId, UUID reservaId) {
        unidadPorId(unidadId).reservarPreventa(reservaId);
    }

    public void liberarReservaPreventa(UUID unidadId) {
        unidadPorId(unidadId).liberarReservaPreventa();
    }

    /**
     * Recibe una unidad en preventa y le asigna su número de serie.
     */
    public void recibirPreventa(UUID unidadId, String numeroSerie) {
        exigir(numeroSerie != null && !numeroSerie.isBlank(), "El número de serie es obligatorio");
        boolean duplicado = unidades.stream()
                .anyMatch(unidad -> !unidad.getId().equals(unidadId)
                        && numeroSerie.equalsIgnoreCase(unidad.getNumeroSerie()));
        exigir(!duplicado, "Ya existe una unidad con ese número de serie en el componente");
        unidadPorId(unidadId).recibir(numeroSerie);
    }

    public void entrarEnRMA(UUID unidadId) {
        unidadPorId(unidadId).entrarEnRMA();
    }

    public void descartarUnidad(UUID unidadId) {
        unidadPorId(unidadId).descartar();
    }

    public long stockDisponible() {
        return unidades.stream().filter(UnidadInventario::estaDisponible).count();
    }

    public long stockPreventa() {
        return unidades.stream().filter(UnidadInventario::estaEnPreventa).count();
    }

    public boolean estaDisponibleParaVenta() {
        return estadoComercial == EstadoComercial.PUBLICADO && stockDisponible() > 0;
    }

    // ---------- Estado comercial ----------

    /**
     * Oculta el componente de la venta. Es la única operación permitida cuando
     * el componente está dentro de un Build activo (regla 6).
     */
    public void marcarAgotado() {
        exigir(estadoComercial != EstadoComercial.ELIMINADO,
                "Un componente eliminado no puede marcarse como agotado");
        this.estadoComercial = EstadoComercial.AGOTADO;
    }

    /**
     * Vuelve a publicar un componente agotado. No permite revivir uno eliminado.
     */
    public void publicar() {
        exigir(estadoComercial != EstadoComercial.ELIMINADO,
                "Un componente eliminado no puede publicarse");
        this.estadoComercial = EstadoComercial.PUBLICADO;
    }

    /**
     * Borrado lógico. El componente conserva sus unidades e historial, pero deja
     * de estar disponible para la venta.
     */
    public void eliminar() {
        this.estadoComercial = EstadoComercial.ELIMINADO;
    }

    public boolean estaEliminado() {
        return estadoComercial == EstadoComercial.ELIMINADO;
    }

    /**
     * Regla 9: solo el vendedor dueño puede publicar stock, recibir preventas,
     * marcarlo agotado o eliminarlo.
     */
    public void validarPropietario(String vendedorId) {
        exigir(vendedorId != null && !vendedorId.isBlank(), "El vendedor es obligatorio");
        exigir(vendedor.getId().equals(vendedorId),
                "Solo el vendedor dueño puede operar este componente");
    }

    // ---------- Consultas ----------

    public boolean existeNumeroSerie(String numeroSerie) {
        if (numeroSerie == null || numeroSerie.isBlank()) {
            return false;
        }
        return unidades.stream()
                .anyMatch(unidad -> numeroSerie.equalsIgnoreCase(unidad.getNumeroSerie()));
    }

    /** Reglas 3, 5 y 8: con garantía vigente y vendedor autorizado. */
    public boolean admiteRMA(LocalDate fechaCompra, LocalDate hoy) {
        return garantia != null && vendedor.esAutorizado() && garantia.estaVigente(fechaCompra, hoy);
    }

    public Optional<Garantia> getGarantia() {
        return Optional.ofNullable(garantia);
    }

    public List<UnidadInventario> getUnidades() {
        return Collections.unmodifiableList(unidades);
    }

    public String getId() {
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

    public Vendedor getVendedor() {
        return vendedor;
    }

    public EstadoComercial getEstadoComercial() {
        return estadoComercial;
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
