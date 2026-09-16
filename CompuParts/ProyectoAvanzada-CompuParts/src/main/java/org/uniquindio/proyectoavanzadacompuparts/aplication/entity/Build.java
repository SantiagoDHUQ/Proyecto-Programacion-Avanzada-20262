package org.uniquindio.proyectoavanzadacompuparts.aplication.entity;


import org.uniquindio.proyectoavanzadacompuparts.aplication.exception.ReglaDominioException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa un armado de PC: un conjunto de Componentes
 * elegidos por un Comprador que debe pasar el Chequeo de Compatibilidad
 * antes de poder comprarse.
 */
public class Build {

    public enum EstadoBuild { EN_ARMADO, LISTO_PARA_COMPRA, INCOMPATIBLE }

    private final UUID id;
    private final List<Componente> componentes;
    private EstadoBuild estado;

    public Build() {
        this.id = UUID.randomUUID();
        this.componentes = new ArrayList<>();
        this.estado = EstadoBuild.EN_ARMADO;
    }

    public void agregarComponente(Componente componente) {
        this.componentes.add(Objects.requireNonNull(componente, "El componente no puede ser nulo"));
        this.estado = EstadoBuild.EN_ARMADO;
    }

    public boolean esCompleto() {
        boolean tieneCpu = componentes.stream().anyMatch(c -> c.getCategoria().equalsIgnoreCase("CPU"));
        boolean tieneMotherboard = componentes.stream().anyMatch(c -> c.getCategoria().equalsIgnoreCase("MOTHERBOARD"));
        boolean tienePsu = componentes.stream().anyMatch(c -> c.getCategoria().equalsIgnoreCase("PSU"));
        return tieneCpu && tieneMotherboard && tienePsu;
    }

    /**
     * Regla de dominio: la PSU del armado debe soportar el consumo total
     * de los demás componentes.
     */
    public void validarCompatibilidad() {
        int consumoTotal = componentes.stream()
                .filter(c -> !c.getCategoria().equalsIgnoreCase("PSU"))
                .mapToInt(c -> c.getEspecificacion().wattajeRequerido())
                .sum();

        int wattajePsu = componentes.stream()
                .filter(c -> c.getCategoria().equalsIgnoreCase("PSU"))
                .mapToInt(c -> c.getEspecificacion().wattajeRequerido())
                .findFirst()
                .orElse(0);

        if (wattajePsu < consumoTotal) {
            this.estado = EstadoBuild.INCOMPATIBLE;
            throw new ReglaDominioException(
                    "La fuente de poder (" + wattajePsu + "W) no soporta el consumo total del armado ("
                            + consumoTotal + "W)");
        }
    }

    public void marcarComoListoParaCompra() {
        if (!esCompleto()) {
            throw new ReglaDominioException(
                    "El armado debe tener al menos CPU, Motherboard y PSU para poder comprarse");
        }
        validarCompatibilidad();
        this.estado = EstadoBuild.LISTO_PARA_COMPRA;
    }

    public UUID getId() {
        return id;
    }

    public List<Componente> getComponentes() {
        return List.copyOf(componentes);
    }

    public EstadoBuild getEstado() {
        return estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Build)) return false;
        Build build = (Build) o;
        return id.equals(build.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}