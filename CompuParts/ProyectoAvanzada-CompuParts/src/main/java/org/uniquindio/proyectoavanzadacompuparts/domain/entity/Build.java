package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import lombok.Getter;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;

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
     * Invariante del agregado: un build solo puede agregarse si no es nulo; después se revalida el estado.
     */
    public void agregarComponente(Componente componente) {
        exigir(componente != null, "El componente no puede ser nulo");
        this.componentes.add(componente);
        this.estado = EstadoBuild.EN_CONSTRUCCION;
    }

    public boolean esCompleto() {
        boolean tieneCpu = componentes.stream().anyMatch(c -> c.getCategoria() == CategoriaComponente.CPU);
        boolean tieneMotherboard = componentes.stream().anyMatch(c -> c.getCategoria() == CategoriaComponente.MOTHERBOARD);
        boolean tienePsu = componentes.stream().anyMatch(c -> c.getCategoria() == CategoriaComponente.PSU);
        return tieneCpu && tieneMotherboard && tienePsu;
    }

    /**
     * Invariante del agregado: la suma de consumo de todos los componentes no debe exceder el wattaje del PSU.
     */
    public void validarCompatibilidad() {
        if (componentes.isEmpty()) {
            throw new ReglaDominioException("El Build no puede validarse sin componentes");
        }

        int consumoTotal = componentes.stream()
                .filter(c -> c.getCategoria() != CategoriaComponente.PSU)
                .mapToInt(c -> c.getEspecificacion().wattajeRequerido())
                .sum();

        int wattajePsu = componentes.stream()
                .filter(c -> c.getCategoria() == CategoriaComponente.PSU)
                .mapToInt(c -> c.getEspecificacion().wattajeRequerido())
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException("El Build necesita una PSU para validarse"));

        if (wattajePsu < consumoTotal) {
            throw new ReglaDominioException(
                    "La PSU del Build tiene un wattaje insuficiente: "
                            + wattajePsu + "W para un consumo de "
                            + consumoTotal + "W"
            );
        }

        boolean incompatibilidadSocket = componentes.stream()
                .filter(c -> c.getCategoria() == CategoriaComponente.CPU || c.getCategoria() == CategoriaComponente.MOTHERBOARD)
                .map(Componente::getEspecificacion)
                .map(especificacion -> especificacion.socket())
                .distinct()
                .count() > 1;

        if (incompatibilidadSocket) {
            this.estado = EstadoBuild.INCOMPATIBLE; // Al fallar el build su estado se establece como INCOMPATIBLE
            throw new ReglaDominioException("El Build tiene incompatibilidades sin resolver antes de comprar");
        }

        this.estado = EstadoBuild.EN_CONSTRUCCION;
    }

    public void marcarComoListoParaCompra() {
        if (!esCompleto()) {
            throw new ReglaDominioException("El Build debe tener al menos CPU, Motherboard y PSU para poder comprarse");
        }
        validarCompatibilidad();
        this.estado = EstadoBuild.LISTO_PARA_COMPRA;
    }

    public Componente componentePorId(String id) {
        return componentes.stream().filter(c -> c.getId().equals(id)).findFirst()
                .orElseThrow(()-> new ReglaDominioException("No existe ese componente"));
    }

    public void eliminarComponente(String id) {
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