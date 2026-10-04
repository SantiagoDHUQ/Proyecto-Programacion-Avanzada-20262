package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Solicitud de garantía asociada a un Componente con falla de fábrica y en vigencia.
 */
public class SolicitudRMA {

    private final String id;
    private final Componente componente;
    private final String numeroSerie;
    private final LocalDate fechaSolicitud;

    private SolicitudRMA(String id, Componente componente, String numeroSerie, LocalDate fechaSolicitud) {
        this.id = Objects.requireNonNull(id, "El identificador no puede ser nulo");
        this.componente = Objects.requireNonNull(componente, "El componente no puede ser nulo");
        this.numeroSerie = Objects.requireNonNull(numeroSerie, "El número de serie no puede ser nulo");
        this.fechaSolicitud = Objects.requireNonNull(fechaSolicitud, "La fecha de solicitud no puede ser nula");
        if (numeroSerie.isBlank()) {
            throw new ReglaDominioException("El número de serie de la SolicitudRMA no puede estar vacío");
        }
        if (componente.isUsadoSinGarantia()) {
            throw new ReglaDominioException("Un componente usado sin garantía nunca puede tener una SolicitudRMA");
        }
    }

    public static SolicitudRMA crear(String id, Componente componente, String numeroSerie, LocalDate fechaSolicitud, Vendedor vendedor) {
        if (!componente.getGarantia().estaVigente(fechaSolicitud) && vendedorAutorizado(vendedor)) {
            throw new ReglaDominioException("La SolicitudRMA solo puede crearse antes del vencimiento de la garantía");
        }
        return new SolicitudRMA(id, componente, numeroSerie, fechaSolicitud);
    }

    public static boolean vendedorAutorizado(Vendedor vendedor){
        if(!vendedor.esAutorizado()){
            throw new ReglaDominioException("El vendedor no está autorizado");
        }
        return true;
    }

    public String getId() {
        return id;
    }

    public Componente getComponente() {
        return componente;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof SolicitudRMA solicitudRMA)) return false;
        return id. equals(solicitudRMA.id);
    }

    @Override
    public int hashCode(){return Objects.hash(id);}
}