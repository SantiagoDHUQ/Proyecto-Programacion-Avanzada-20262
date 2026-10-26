package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import lombok.Getter;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ResultadoCompatibilidad;

import java.util.*;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Agregado Build: dentro de este agregado viven los Componentes que forman el armado y la evaluación de compatibilidad.
 * 
 * Invariantes que garantiza:
 *  - Un Build siempre nace en estado EN_CONSTRUCCION.
 *  - Solo se puede eliminar un componente si el estado NO es LISTO_PARA_COMPRA.
 *  - Para marcar como LISTO_PARA_COMPRA, el Build debe ser completo (CPU, Motherboard, PSU) y validar la compatibilidad (consumo PSU y compatibilidad de socket).
 *  - La suma de consumo de todos los componentes no debe exceder el wattaje del PSU.
 */
@Getter
public class Build {

    private final UUID id;
    private final List<Componente> componentes;
    private EstadoBuild estado;

    private Build() {
        this.id = UUID.randomUUID();
        this.componentes = new ArrayList<>();
        this.estado = EstadoBuild.EN_CONSTRUCCION;
    }

    public static Build crear() {
        return new Build();
    }

    /**
     * Invariante del agregado: un Build no repite el mismo SKU y solo acepta
     * componentes publicados y con stock disponible. Agregar no reserva stock:
     * la reserva ocurre al realizar la compra.
     */
    public void agregarComponente(Componente componente) {
        exigir(estaActivo(), "Un Build comprado o cancelado no puede modificarse");
        exigir(componente != null, "El componente no puede ser nulo");
        exigir(!contieneComponente(componente.getId()), "El Build ya contiene ese componente");
        exigir(componente.estaDisponibleParaVenta(),
                "El componente no está disponible para la venta (sin stock, agotado, eliminado o en preventa)");
        this.componentes.add(componente);
        this.estado = EstadoBuild.EN_CONSTRUCCION;
    }

    public boolean contieneComponente(String componenteId) {
        return componentes.stream().anyMatch(componente -> componente.getId().equals(componenteId));
    }

    public boolean todosLosComponentesDisponibles() {
        return componentes.stream().allMatch(Componente::estaDisponibleParaVenta);
    }

    /** Un Build sigue activo mientras no se haya comprado ni cancelado. */
    public boolean estaActivo() {
        return estado != EstadoBuild.COMPRADO && estado != EstadoBuild.CANCELADO;
    }

    public boolean esCompleto() {
        boolean tieneCpu = componentes.stream().anyMatch(c -> c.getCategoria() == CategoriaComponente.CPU);
        boolean tieneMotherboard = componentes.stream().anyMatch(c -> c.getCategoria() == CategoriaComponente.MOTHERBOARD);
        boolean tienePsu = componentes.stream().anyMatch(c -> c.getCategoria() == CategoriaComponente.PSU);
        return tieneCpu && tieneMotherboard && tienePsu;
    }

    /**
     * Invariante del agregado: la suma de consumo de todos los componentes no debe exceder el wattaje del PSU.
     * <p>
     * Devuelve el resultado con los motivos en lugar de lanzar una excepción: una incompatibilidad es un
     * resultado esperado del negocio, no un fallo del programa. Actualiza el estado del Build.
     */
    public ResultadoCompatibilidad evaluarCompatibilidad() {
        if (!estaActivo()) {
            return ResultadoCompatibilidad.ok();
        }
        if (componentes.isEmpty()) {
            return ResultadoCompatibilidad.incompatible("El Build no puede validarse sin componentes");
        }

        if (this.estado == EstadoBuild.LISTO_PARA_COMPRA) {
            return ResultadoCompatibilidad.ok();
        }

        List<String> motivos = new ArrayList<>();

        int consumoTotal = componentes.stream()
                .filter(c -> c.getCategoria() != CategoriaComponente.PSU)
                .mapToInt(c -> c.getEspecificacion().wattajeRequerido())
                .sum();

        Integer wattajePsu = componentes.stream()
                .filter(c -> c.getCategoria() == CategoriaComponente.PSU)
                .map(c -> c.getEspecificacion().wattajeRequerido())
                .findFirst()
                .orElse(null);

        if (wattajePsu == null) {
            motivos.add("El Build necesita una PSU para validarse");
        } else if (wattajePsu < consumoTotal) {
            motivos.add("La PSU del Build tiene un wattaje insuficiente: "
                    + wattajePsu + "W para un consumo de " + consumoTotal + "W");
        }

        if (hayIncompatibilidadDeSocket()) {
            motivos.add("El Build tiene incompatibilidades de socket sin resolver");
        }

        if (hayIncompatibilidadDeMemoria()) {
            motivos.add("El Build tiene memorias RAM incompatibles con la motherboard");
        }

        if (motivos.isEmpty()) {
            this.estado = EstadoBuild.EN_CONSTRUCCION;
            return ResultadoCompatibilidad.ok();
        }

        this.estado = EstadoBuild.INCOMPATIBLE;
        return ResultadoCompatibilidad.incompatible(motivos);
    }

    /**
     * Compatibilidad estricta: lanza si el Build no es compatible.
     * Se conserva para los flujos que necesitan fallar (por ejemplo, marcar listo para compra).
     */
    public void validarCompatibilidad() {
        ResultadoCompatibilidad resultado = evaluarCompatibilidad();
        if (!resultado.compatible()) {
            throw new ReglaDominioException(String.join("; ", resultado.motivos()));
        }
    }

    public boolean hayIncompatibilidadDeSocket() {
        return componentes.stream()
                .filter(c -> c.getCategoria() == CategoriaComponente.CPU || c.getCategoria() == CategoriaComponente.MOTHERBOARD)
                .map(Componente::getEspecificacion)
                .map(especificacion -> especificacion.socket())
                .filter(Objects::nonNull)
                .distinct()
                .count() > 1;
    }

    /**
     * La motherboard fija el tipo de memoria (DDR4/DDR5). Si hay RAM con otro
     * tipo, el Build es incompatible. Los componentes sin tipo de memoria no
     * participan en esta comparación.
     */
    public boolean hayIncompatibilidadDeMemoria() {
        String tipoPlaca = componentes.stream()
                .filter(c -> c.getCategoria() == CategoriaComponente.MOTHERBOARD)
                .map(c -> c.getEspecificacion().tipoMemoria())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (tipoPlaca == null) {
            return false;
        }

        return componentes.stream()
                .filter(c -> c.getCategoria() == CategoriaComponente.RAM)
                .map(c -> c.getEspecificacion().tipoMemoria())
                .filter(Objects::nonNull)
                .anyMatch(tipo -> !tipo.equals(tipoPlaca));
    }

    public void marcarComoListoParaCompra() {
        exigir(estaActivo(), "Un Build comprado o cancelado no puede modificarse");
        if (!esCompleto()) {
            throw new ReglaDominioException("El Build debe tener al menos CPU, Motherboard y PSU para poder comprarse");
        }
        ResultadoCompatibilidad resultado = evaluarCompatibilidad();
        if (!resultado.compatible()) {
            throw new ReglaDominioException(String.join("; ", resultado.motivos()));
        }
        if (!todosLosComponentesDisponibles()) {
            this.estado = EstadoBuild.EN_CONSTRUCCION;
            throw new ReglaDominioException("El Build tiene componentes sin stock disponible");
        }
        this.estado = EstadoBuild.LISTO_PARA_COMPRA;
    }

    /** Marca el Build como comprado cuando la compra asociada se confirma. */
    public void marcarComoComprado() {
        exigir(estado == EstadoBuild.LISTO_PARA_COMPRA,
                "Solo un Build listo para compra puede marcarse como comprado");
        this.estado = EstadoBuild.COMPRADO;
    }

    public Componente componentePorId(String id) {
        return componentes.stream().filter(c -> c.getId().equals(id)).findFirst()
                .orElseThrow(()-> new ReglaDominioException("No existe ese componente"));
    }

    public void eliminarComponente(String id) {
        exigir(estaActivo(), "Un Build comprado o cancelado no puede modificarse");
        if (this.estado == EstadoBuild.LISTO_PARA_COMPRA) {
            throw new ReglaDominioException("No se puede eliminar un componente de un Build listo para compra");
        }
        Componente componente = componentePorId(id);
        componentes.remove(componente);
        this.estado = EstadoBuild.EN_CONSTRUCCION;
    }

    public List<Componente> getComponentes() {
        return Collections.unmodifiableList(componentes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Build build)) return false;
        return id.equals(build.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}