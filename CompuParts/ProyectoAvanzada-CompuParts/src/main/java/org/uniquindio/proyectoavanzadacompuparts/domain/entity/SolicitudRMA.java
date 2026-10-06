package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import lombok.Getter;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Solicitud de garantía asociada a un Componente con falla de fábrica y en vigencia.
 */
@Getter
public class SolicitudRMA {

    private final String id;
    private final Componente componente;
    private final String numeroSerie;
    private final LocalDate fechaSolicitud;

    private SolicitudRMA(String id, Componente componente, LocalDate fechaSolicitud) {
        this.id = Objects.requireNonNull(id, "El identificador no puede ser nulo");
        this.componente = Objects.requireNonNull(componente, "El componente no puede ser nulo");
        this.numeroSerie = componente.getNumeroSerie();
        this.fechaSolicitud = Objects.requireNonNull(fechaSolicitud, "La fecha de solicitud no puede ser nula");
        if (numeroSerie.isBlank()) {
            throw new ReglaDominioException("El número de serie de la SolicitudRMA no puede estar vacío");
        }
        if (componente.admiteRMA()) {
            throw new ReglaDominioException("");
        }
    }

    public static SolicitudRMA crear(String id, Componente componente, LocalDate fechaSolicitud) {
        if (!componente.admiteRMA()) {
            throw new ReglaDominioException("La SolicitudRMA no puede crearse después del vencimiento de la garantía");
        }
        return new SolicitudRMA(id, componente, fechaSolicitud);
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