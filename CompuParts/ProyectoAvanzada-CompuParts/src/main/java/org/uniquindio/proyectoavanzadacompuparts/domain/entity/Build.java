package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Build: dentro de este agregado viven los Componentes que forman el armado y la evaluación de compatibilidad.
 * SolicitudRMA NO vive dentro del agregado, se referencia fuera de él.
 */
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
        this.componentes.add(Objects.requireNonNull(componente, "El componente no puede ser nulo"));
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
            this.estado = EstadoBuild.INCOMPATIBLE;
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
            this.estado = EstadoBuild.INCOMPATIBLE;
            throw new ReglaDominioException(
                    "La PSU del Build tiene un wattaje insuficiente: " + wattajePsu + "W para un consumo de " + consumoTotal + "W");
        }

        boolean incompatibilidadSocket = componentes.stream()
                .filter(c -> c.getCategoria() == CategoriaComponente.CPU || c.getCategoria() == CategoriaComponente.MOTHERBOARD)
                .map(Componente::getEspecificacion)
                .map(especificacion -> especificacion.socket())
                .distinct()
                .count() > 1;

        if (incompatibilidadSocket) {
            this.estado = EstadoBuild.INCOMPATIBLE;
            throw new ReglaDominioException("El Build tiene incompatibilidades sin resolver antes de comprar");
        }

        this.estado = EstadoBuild.EN_CONSTRUCCION;
    }

    public void marcarComoListoParaCompra() {
        if (!esCompleto()) {
            throw new ReglaDominioException("El Build debe tener al menos CPU, Motherboard y PSU para poder comprarse");
        }
        try {
            validarCompatibilidad();
        } catch (ReglaDominioException e) {
            throw e;
        }
        this.estado = EstadoBuild.LISTO_PARA_COMPRA;
    }

    public UUID getId() {
        return id;
    }

    public List<Componente> getComponentes() {
        return Collections.unmodifiableList(componentes);
    }

    public EstadoBuild getEstado() {
        return estado;
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
