package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Solicitud de garantía asociada a un Componente con falla de fábrica y en vigencia.
 */
public class SolicitudRMA {

    private final UUID id;
    private final Componente componente;
    private final String numeroSerie;
    private final LocalDate fechaSolicitud;

    private SolicitudRMA(Componente componente, String numeroSerie, LocalDate fechaSolicitud) {
        this.id = UUID.randomUUID();
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

    public static SolicitudRMA crear(Componente componente, String numeroSerie, LocalDate fechaSolicitud) {
        LocalDate fechaActual = LocalDate.now();
        if (!componente.getGarantia().estaVigente(fechaActual)) {
            throw new ReglaDominioException("La SolicitudRMA solo puede crearse antes del vencimiento de la garantía");
        }
        return new SolicitudRMA(componente, numeroSerie, fechaSolicitud);
    }

    public UUID getId() {
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
}
